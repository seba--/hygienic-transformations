package lang.lambda.module

import lang.lambda.Exp
import name._

abstract class Module extends Nominal {
  val id : String
  val imports: Set[Module]
  val defs: Map[(Name, Boolean), Exp]

  override def rename(renaming: Renaming) = rename(defs.map(d => ((renaming(d._1._1), d._1._2), d._2.rename(renaming))).toMap)
  def rename(renamingResult : Map[(Name, Boolean), Exp]) : Nominal

  override def allNames = defs.values.foldLeft(Set[Name.ID]())(_ ++ _.allNames) ++ defs.keys.map(_._1.id)

  override def resolveNames = {
    val moduleNodes = defs.keys.map(d => (d._1.id, d._2)).toSet

    // Find imported names with same strings and create a set of conflicting name sets.
    val importConflicts = imports.flatMap(m => m.exportedNames.map(n => (m.id, n))).foldLeft(Set[Set[(String, Name.ID)]]())(
      (oldSet, d) => oldSet.find(_.exists(d._2.name == _._2.name)) match {
        case Some(set) => oldSet - set + (set + d)
        case None => oldSet + Set(d)
      }).filter(_.size > 1)

    val doubleDefNames = defs.foldLeft(Set[Set[Name.ID]]())((oldSet, d) => oldSet.find(_.exists(d._1._1.name == _.name)) match {
      case Some(set) => oldSet - set + (set + d._1._1.id)
      case None => oldSet + Set(d._1._1.id)
    }).filter(_.size > 1)

    val moduleGraph = defs.foldLeft(NameGraphGlobal(moduleNodes, Map(), doubleDefNames))(_ ++ _._2.resolveNames(moduleScope))

    val externalRefs = moduleGraph.E.filter(e => !allNames.contains(e._2)).map(e => (e._1, (imports.find(_.exportedNames.contains(e._2)).get.id, e._2))).toMap

    NameGraphModular(id, moduleGraph.V, moduleGraph.E -- externalRefs.keys, externalRefs, moduleGraph.C, importConflicts)
  }

  def moduleScope : Map[String, Name.ID]

  def exportedNames : Set[Name.ID]
}

case class InternalPrecedenceModule(id: String, imports: Set[Module], defs: Map[(Name, Boolean), Exp]) extends Module {
  override def moduleScope = {
    val importedScope = imports.foldLeft(Set[Name.ID]())(_ ++ _.exportedNames).map(name => (name.name, name)).toMap
    val internalScope = defs.keys.foldLeft(Set[Name]())(_ + _._1).map(name => (name.name, name.id)).toMap
    importedScope ++ internalScope
  }

  override def rename(renamingResult : Map[(Name, Boolean), Exp]) = InternalPrecedenceModule(id, imports, renamingResult)

  override def exportedNames = imports.foldLeft(Set[Name.ID]())(_ ++ _.exportedNames) ++ defs.keys.filter(_._2).map(_._1.id).toSet
}


case class ExternalPrecedenceModule(id: String, imports: Set[Module], defs: Map[(Name, Boolean), Exp]) extends Module {
  override def moduleScope = {
    val importedScope = imports.foldLeft(Set[Name.ID]())(_ ++ _.exportedNames).map(name => (name.name, name)).toMap
    val internalScope = defs.keys.foldLeft(Set[Name]())(_ + _._1).map(name => (name.name, name.id)).toMap
    internalScope ++ importedScope
  }

  override def rename(renamingResult : Map[(Name, Boolean), Exp]) = InternalPrecedenceModule(id, imports, renamingResult)

  override def exportedNames = defs.keys.filter(_._2).map(_._1.id).toSet ++ imports.foldLeft(Set[Name.ID]())(_ ++ _.exportedNames)
}


case class NoPrecedenceModule(id: String, imports: Set[Module], defs: Map[(Name, Boolean), Exp]) extends Module {
  override def resolveNames = {
    val moduleNodes = defs.keys.map(d => (d._1.id, d._2)).toSet

    val importNames = imports.flatMap(m => m.exportedNames.map(n => (m.id, n))).foldLeft(Set[Set[(String, Name.ID)]]())(
      (oldSet, d) => oldSet.find(_.exists(d._2.name == _._2.name)) match {
        case Some(set) => oldSet - set + (set + d)
        case None => oldSet + Set(d)
      })

    val doubleDefNames = defs.foldLeft(importNames.foldLeft(Set[Set[Name.ID]]())(_ + _.map(_._2)))(
      (oldSet, d) => oldSet.find(_.exists(d._1._1.name == _.name)) match {
      case Some(set) => oldSet - set + (set + d._1._1.id)
      case None => oldSet + Set(d._1._1.id)
    }).filter(_.size > 1)

    val moduleGraph = defs.foldLeft(NameGraphGlobal(moduleNodes, Map(), doubleDefNames))(_ ++ _._2.resolveNames(moduleScope))

    val externalRefs = moduleGraph.E.filter(e => !allNames.contains(e._2)).map(e => (e._1, (imports.find(_.exportedNames.contains(e._2)).get.id, e._2))).toMap

    NameGraphModular(id, moduleGraph.V, moduleGraph.E -- externalRefs.keys, externalRefs, moduleGraph.C, importNames.filter(_.size > 1))
  }

  override def moduleScope = {
    val importedScope = imports.foldLeft(Set[Name.ID]())(_ ++ _.exportedNames).map(name => (name.name, name)).toMap
    val internalScope = defs.keys.foldLeft(Set[Name]())(_ + _._1).map(name => (name.name, name.id)).toMap
    importedScope ++ internalScope
  }

  override def rename(renamingResult : Map[(Name, Boolean), Exp]) = NoPrecedenceModule(id, imports, renamingResult)

  override def exportedNames = defs.keys.filter(_._2).map(_._1.id).toSet ++ imports.foldLeft(Set[Name.ID]())(_ ++ _.exportedNames)
}