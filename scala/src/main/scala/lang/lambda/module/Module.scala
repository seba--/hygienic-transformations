package lang.lambda.module

import lang.lambda.Exp
import name._

abstract class Module extends Nominal {
  val name : Name
  val imports: Set[Module]
  val defs: Map[(Name, Boolean), Exp]

  override def rename(renaming: Renaming) = rename(defs.map(d => ((renaming(d._1._1), d._1._2), d._2.rename(renaming))).toMap)
  def rename(renamingResult : Map[(Name, Boolean), Exp]) : Nominal

  override def allNames = defs.values.foldLeft(Set[Name.ID]())(_ ++ _.allNames) ++ defs.keys.map(_._1.id)

  override def resolveNames = {
    val moduleNodes = defs.keys.map(d => (d._1.id, d._2)).toSet

    // Find imported names with same strings and create a set of conflicting name sets.
    val importConflicts = imports.flatMap(m => m.exportedNames).foldLeft(Set[Set[Name.ID]]())(
      (oldSet, d) => oldSet.find(_.exists(d.name == _.name)) match {
        case Some(set) => oldSet - set + (set + d)
        case None => oldSet + Set(d)
      }).filter(_.size > 1)

    val doubleDefNames = defs.foldLeft(Set[Set[Name.ID]]())((oldSet, d) => oldSet.find(_.exists(d._1._1.name == _.name)) match {
      case Some(set) => oldSet - set + (set + d._1._1.id)
      case None => oldSet + Set(d._1._1.id)
    }).filter(_.size > 1)

    val moduleGraph = defs.foldLeft(NameGraphGlobal(moduleNodes, Map(), doubleDefNames))(_ ++ _._2.resolveNames(moduleScope))

    val externalRefs = moduleGraph.E.filter(e => !allNames.contains(e._2)).map(e => (e._1, (imports.find(_.exportedNames.contains(e._2)).get.name.id, e._2))).toMap

    NameGraphModular(name.id, moduleGraph.V, moduleGraph.E -- externalRefs.keys, externalRefs, moduleGraph.C ++ importConflicts)
  }

  def moduleScope : Map[String, Name.ID]

  def exportedNames : Set[Name.ID] = defs.keys.filter(_._2).map(_._1.id).toSet
}

case class InternalPrecedenceModule(name: Name, imports: Set[Module], defs: Map[(Name, Boolean), Exp]) extends Module {
  override def moduleScope = {
    val importedScope = imports.foldLeft(Set[Name.ID]())(_ ++ _.exportedNames).map(name => (name.name, name)).toMap
    val internalScope = defs.keys.foldLeft(Set[Name]())(_ + _._1).map(name => (name.name, name.id)).toMap
    importedScope ++ internalScope
  }

  override def rename(renamingResult : Map[(Name, Boolean), Exp]) = InternalPrecedenceModule(name, imports, renamingResult)
}


case class ExternalPrecedenceModule(name: Name, imports: Set[Module], defs: Map[(Name, Boolean), Exp]) extends Module {
  override def moduleScope = {
    val importedScope = imports.foldLeft(Set[Name.ID]())(_ ++ _.exportedNames).map(name => (name.name, name)).toMap
    val internalScope = defs.keys.foldLeft(Set[Name]())(_ + _._1).map(name => (name.name, name.id)).toMap
    internalScope ++ importedScope
  }

  override def rename(renamingResult : Map[(Name, Boolean), Exp]) = ExternalPrecedenceModule(name, imports, renamingResult)
}


case class NoPrecedenceModule(name: Name, imports: Set[Module], defs: Map[(Name, Boolean), Exp]) extends Module {
  override def resolveNames = {
    val moduleNodes = defs.keys.map(d => (d._1.id, d._2)).toSet

    val importNames = imports.flatMap(m => m.exportedNames).foldLeft(Set[Set[Name.ID]]())(
      (oldSet, d) => oldSet.find(_.exists(d.name == _.name)) match {
        case Some(set) => oldSet - set + (set + d)
        case None => oldSet + Set(d)
      })

    val doubleDefNames = defs.foldLeft(importNames)(
      (oldSet, d) => oldSet.find(_.exists(d._1._1.name == _.name)) match {
      case Some(set) => oldSet - set + (set + d._1._1.id)
      case None => oldSet + Set(d._1._1.id)
    }).filter(_.size > 1)

    val moduleGraph = defs.foldLeft(NameGraphGlobal(moduleNodes, Map(), doubleDefNames))(_ ++ _._2.resolveNames(moduleScope))

    val externalRefs = moduleGraph.E.filter(e => !allNames.contains(e._2)).map(e => (e._1, (imports.find(_.exportedNames.contains(e._2)).get.name.id, e._2))).toMap

    NameGraphModular(name.id, moduleGraph.V, moduleGraph.E -- externalRefs.keys, externalRefs, moduleGraph.C)
  }

  override def moduleScope = {
    val importedScope = imports.foldLeft(Set[Name.ID]())(_ ++ _.exportedNames).map(name => (name.name, name)).toMap
    val internalScope = defs.keys.foldLeft(Set[Name]())(_ + _._1).map(name => (name.name, name.id)).toMap
    importedScope ++ internalScope
  }

  override def rename(renamingResult : Map[(Name, Boolean), Exp]) = NoPrecedenceModule(name, imports, renamingResult)
}