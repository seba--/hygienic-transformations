package lang.lambda.module

import lang.lambda.Exp
import lang.lambda.module.Module.Def
import name.namegraph.{NameGraphExtended, NameGraphModular}
import name.{Renaming, Name, NominalModular, Identifier}

object Module {
  // Definition body and modifier marking the def as exported
  type Def = (Exp, Boolean)
}

// name = name of the the module (currently not used but may be interesting when considering qualifiers)
// imports = set of imported module identifiers; All exported definitions of imported modules are made available in the module
// defs = local definitions available to each other and can be marked as exported to become part of the module interface
case class Module(name: Identifier, imports:Set[Identifier], defs: Map[Identifier, Def]) extends NominalModular[ModuleInterface] {
  private var _resolved: NameGraphModular[ModuleInterface] = _
  private var dependencies = Set[ModuleInterface]()

  override def link(dependencies: Set[ModuleInterface]) = {
    this.dependencies = dependencies
    _resolved = null
    this
  }

  override def allNames: Set[Name] = imports.map(_.name) ++ defs.flatMap(d => d._2._1.allNames + d._1.name) + name.name

  override def resolveNamesModular: NameGraphModular[ModuleInterface] = {
    if (_resolved != null)
      return _resolved

    // Resolves edges from imports to (string-wise) matching exported identifiers in used interfaces.
    // If multiple names match, references to all of them are added to represent the ambiguity (=> imports have no precedence!)
    val importEdges = imports.map(i => (i, dependencies.map(_.moduleID).groupBy(_.name).find(_._1 == i.name))).collect {
      case (v, Some(d)) => v -> d._2
    }.toMap


    // Resolves edges from conflicting definitions
    val defConflicts = defs.keys.groupBy(_.name).filter(_._2.size > 1).map(_._2.toSet)
    val importConflicts = imports.groupBy(_.name).filter(_._2.size > 1).map(_._2.toSet)
    var conflictEdges = Map[Identifier, Set[Identifier]]()
    for (conflict <- defConflicts ++ importConflicts) {
      conflictEdges ++= conflict.map(id => (id, conflict - id)).toMap
    }

    // Generates the "module-global" scope that is used for all defs.
    // As defs are added after imports, internal names have precedence over external ones
    val importedDefs = dependencies.filter(d => importEdges.values.flatten.toSet.contains(d.moduleID)).flatMap(_.exportedDefs).groupBy(_.name)
    val moduleScope = importedDefs ++ defs.map(d => d._1.name -> Set(d._1))

    var moduleGraph = NameGraphExtended(imports ++ defs.keys + name, importEdges)
    moduleGraph += NameGraphExtended(conflictEdges)
    for ((defName, (defExpr, exported)) <- defs) {
      moduleGraph += defExpr.resolveNames(moduleScope)
    }

    _resolved = NameGraphModular(moduleGraph.V, dependencies, moduleGraph.E, interface)
    _resolved
  }

  override def rename(renaming: Renaming): NominalModular[ModuleInterface] = {
    val m = Module(renaming(name), imports.map(renaming(_)), defs.map(d => (renaming(d._1), (d._2._1.rename(renaming), d._2._2))))
    m.link(dependencies)
    m
  }

  val interface: ModuleInterface = ModuleInterface(name, defs.filter(_._2._2).keys.toSet)
}
