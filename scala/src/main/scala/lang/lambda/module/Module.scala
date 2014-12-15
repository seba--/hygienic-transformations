package lang.lambda.module

import lang.lambda.{QualifiedVar, Exp}
import name._

abstract class Module extends NominalModular {
  val name : Name
  val imports: Set[Module]
  val defs: Map[(Name, Boolean), Exp]

  override def rename(renaming: Renaming) = copyWithDefs(defs.map(d => ((renaming(d._1._1), d._1._2), d._2.rename(renaming))).toMap)
  def copyWithDefs(newDefs : Map[(Name, Boolean), Exp]) : Module

  override def allNames = defs.values.foldLeft(Set[Name.ID]())(_ ++ _.allNames) ++ defs.keys.map(_._1.id)

  override def resolveNames(dependencyRenaming : DependencyRenaming) = {
    val moduleNodes = defs.keys.map(_._1.id).toSet

    // Find imported names with same strings and create a set of conflicting name sets.
    val importConflicts = imports.flatMap(m => m.exportedNames.map(_.id)).foldLeft(Set[Set[Name.ID]]())(
      (oldSet, d) => oldSet.find(_.exists(d.name == _.name)) match {
        case Some(set) => oldSet - set + (set + d)
        case None => oldSet + Set(d)
      }).filter(_.size > 1)

    val doubleDefNames = defs.foldLeft(Set[Set[Name.ID]]())((oldSet, d) => oldSet.find(_.exists(d._1._1.name == _.name)) match {
      case Some(set) => oldSet - set + (set + d._1._1.id)
      case None => oldSet + Set(d._1._1.id)
    }).filter(_.size > 1)

    val modularScope = defs.map(d => ((name.name, d._1._1.name), (name.id, d._1._1.id))).toMap ++ imports.flatMap(i => i.exportedNames.map(n => ((i.name.name, n.name), (i.name.id, n.id)))).toMap

    val moduleGraph = defs.foldLeft(NameGraphGlobal(moduleNodes, Map(), doubleDefNames))(_ ++ _._2.resolveNames(internalScope, modularScope))

    val externalRefs = moduleGraph.E.filter(e => !allNames.contains(e._2)).map(e => (e._1, (imports.find(_.exportedNames.exists(_.id == e._2)).get.name.id, e._2))).toMap

    NameGraphModular(name.id, moduleGraph.V, moduleGraph.E -- externalRefs.keys, externalRefs, moduleGraph.C ++ importConflicts)
  }

  protected def internalScope : Map[String, Name.ID]

  override def exportedNames : Set[Name] = defs.keys.filter(_._2).map(_._1).toSet

  override def safelyQualifiedReference(reference: Name, declaration: Name.ID): Option[Module] =
    imports.find(_.exportedNames.exists(_.id == declaration)) match {
      case Some(module) => Some(copyWithDefs(defs.map(d => (d._1, d._2.replaceByQualifiedVar(reference, QualifiedVar(module.name.fresh, reference))))))
      case None =>
        if (defs.exists(_._1._1.id == reference.id)) Some(copyWithDefs(defs.map(d => (d._1, d._2.replaceByQualifiedVar(reference, QualifiedVar(name.fresh, reference))))))
        else None
    }
  
}

case class ModuleInternalPrecedence(name: Name, imports: Set[Module], defs: Map[(Name, Boolean), Exp]) extends Module {
  override def internalScope = {
    val importedScope = imports.foldLeft(Set[Name]())(_ ++ _.exportedNames).map(name => (name.name, name.id)).toMap
    val internalScope = defs.keys.foldLeft(Set[Name]())(_ + _._1).map(name => (name.name, name.id)).toMap
    importedScope ++ internalScope
  }

  override def copyWithDefs(newDefs : Map[(Name, Boolean), Exp]) = ModuleInternalPrecedence(name, imports, newDefs)
}


case class ModuleExternalPrecedence(name: Name, imports: Set[Module], defs: Map[(Name, Boolean), Exp]) extends Module {
  override def internalScope = {
    val importedScope = imports.foldLeft(Set[Name]())(_ ++ _.exportedNames).map(name => (name.name, name.id)).toMap
    val internalScope = defs.keys.foldLeft(Set[Name]())(_ + _._1).map(name => (name.name, name.id)).toMap
    internalScope ++ importedScope
  }

  override def copyWithDefs(newDefs : Map[(Name, Boolean), Exp]) = ModuleExternalPrecedence(name, imports, newDefs)
}


case class ModuleNoPrecedence(name: Name, imports: Set[Module], defs: Map[(Name, Boolean), Exp]) extends Module {
  override def resolveNames = {
    val moduleNodes = defs.keys.map(_._1.id).toSet

    val importNames = imports.flatMap(m => m.exportedNames.map(_.id)).foldLeft(Set[Set[Name.ID]]())(
      (oldSet, d) => oldSet.find(_.exists(d.name == _.name)) match {
        case Some(set) => oldSet - set + (set + d)
        case None => oldSet + Set(d)
      })

    val doubleDefNames = defs.foldLeft(importNames)(
      (oldSet, d) => oldSet.find(_.exists(d._1._1.name == _.name)) match {
      case Some(set) => oldSet - set + (set + d._1._1.id)
      case None => oldSet + Set(d._1._1.id)
    }).filter(_.size > 1)

    val modularScope = defs.map(d => ((name.name, d._1._1.name), (name.id, d._1._1.id))).toMap ++ imports.flatMap(i => i.exportedNames.map(n => ((i.name.name, n.name), (i.name.id, n.id)))).toMap

    val moduleGraph = defs.foldLeft(NameGraphGlobal(moduleNodes, Map(), doubleDefNames))(_ ++ _._2.resolveNames(internalScope, modularScope))

    val externalRefs = moduleGraph.E.filter(e => !allNames.contains(e._2)).map(e => (e._1, (imports.find(_.exportedNames.exists(_.id == e._2)).get.name.id, e._2))).toMap

    NameGraphModular(name.id, moduleGraph.V, moduleGraph.E -- externalRefs.keys, externalRefs, moduleGraph.C)
  }

  override def internalScope = {
    val importedScope = imports.foldLeft(Set[Name]())(_ ++ _.exportedNames).map(name => (name.name, name.id)).toMap
    val internalScope = defs.keys.foldLeft(Set[Name]())(_ + _._1).map(name => (name.name, name.id)).toMap
    importedScope ++ internalScope
  }

  override def copyWithDefs(newDefs : Map[(Name, Boolean), Exp]) = ModuleNoPrecedence(name, imports, newDefs)
}