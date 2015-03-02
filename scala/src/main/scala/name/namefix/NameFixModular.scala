package name.namefix

import name.Gensym._
import name._
import name.namegraph.NameGraphModular
import name.namegraph.NameGraphModular.Nodes

class NameFixModular {
  protected def findRelations(n: Identifier,g: NameGraphModular, result: Nodes = Set()): Nodes = {
    var newResult = result + n

    if (g.E.contains(n))
        newResult ++= (g.E(n) -- newResult).flatMap(d => findRelations(d, g, newResult))
    if (g.EOut.contains(n))
      newResult ++= (g.EOut(n) -- newResult).flatMap(d => findRelations(d, g, newResult))
    for ((v, d) <- g.E if !newResult.contains(v) && d.contains(n))
        newResult ++= findRelations(v, g, newResult)
    for ((v, d) <- g.EOut if !newResult.contains(v) && d.contains(n))
      newResult ++= findRelations(v, g, newResult)

    newResult
  }

  protected def findCapturedNodes(gs: NameGraphModular, gt: NameGraphModular): Nodes = {
    val names = gt.V ++ gt.EOut.values.flatten
    names.filter(v => (gs.V.contains(v) || gs.EOut.values.exists(_.contains(v))) && (findRelations(v, gt) -- findRelations(v, gs)).size > 0)
  }

  protected def compRenamings(gs: NameGraphModular, gt: NameGraphModular, t: Nominal, toRename: Nodes): Map[Identifier, Name] = {
    var renaming: Map[Identifier, Name] = Map()

    for (v <- toRename if findRelations(v, gt).intersect(renaming.keySet).isEmpty) {
      val fresh = gensym(v.name, t.allNames.map(_.name) ++ gt.EOut.values.flatten.map(_.name) ++ renaming.values)
      val relatedNames = findRelations(v, gs)
      renaming ++= relatedNames.map(r => (r, fresh))
    }

    renaming
  }

  protected def nameFixVirtual[S <: Meta, T <: NominalModular[S]](gS: NameGraphModular, mT: T, metaDep: Set[S], virtualRenaming: Map[Identifier, Name]): NameGraphModular = {
    val gT = mT.resolveNamesVirtual(metaDep, virtualRenaming)
    val capture = findCapturedNodes(gS, gT)

    if (capture.isEmpty)
      gT

    val newRenaming = compRenamings(gS, gT, mT, capture)
    nameFixVirtual(gS, mT, metaDep, virtualRenaming ++ newRenaming)
  }

  protected def applyVirtualGraph[S <: Meta, T <: NominalModular[S]](m: T, metaDep: Set[S], gVirtual: NameGraphModular) = {
    val m2 = addIntendedRelations(m, metaDep, gVirtual)
    removeUnintendedRelations(m2, metaDep, gVirtual)
  }

  protected def selectRenaming[S <: Meta](rel1: Nodes, rel2: Nodes, fresh: Name, g: NameGraphModular, meta: S) = {
    val externalNames = g.EOut.values.flatten.toSet

    if ((rel1 ++ rel2).intersect(externalNames).isEmpty) {
      if (rel1.intersect(meta.export).size > rel2.intersect(meta.export).size)
        rel2.map(r => (r, fresh))
      else
        rel1.map(r => (r, fresh))
    }
    else {
      if (rel1.intersect(externalNames).isEmpty)
        rel1.map(r => (r, fresh))
      else if (rel2.intersect(externalNames).isEmpty)
        rel2.map(r => (r, fresh))
      else
        sys.error("Unable to fix module without renaming external identifiers!")
    }
  }

  protected def removeUnintendedRelations[S <: Meta, T <: NominalModular[S]](m: T, metaDep: Set[S], gVirtual: NameGraphModular): (T, S) = {
    val (gM, metaM) = m.resolveNamesModular(metaDep)
    var renaming: Map[Identifier, Name] = Map()

    for (v <- gM.V) {
      val fresh = gensym(v.name, m.allNames.map(_.name) ++ gM.EOut.values.flatten.map(_.name) ++ renaming.values)

      val relM = findRelations(v, gM)
      val relV = findRelations(v, gVirtual)
      val relRemove = (relM -- relV).flatMap(r => findRelations(r, gVirtual))

      if (relRemove.nonEmpty && (relRemove ++ relV).intersect(renaming.keySet).isEmpty)
        renaming ++= selectRenaming(relV, relRemove, fresh, gVirtual, metaM)
    }

    if (renaming.isEmpty)
      (m, metaM)

    val mNew = m.rename(renaming).asInstanceOf[T]

    removeUnintendedRelations(mNew, metaDep, gVirtual)
  }

  protected def addIntendedRelations[S <: Meta, T <: NominalModular[S]](m: T, metaDep: Set[S], gVirtual: NameGraphModular) = {
    val (gM, metaM) = m.resolveNamesModular(metaDep)
    var renaming: Map[Identifier, Name] = Map()

    for (v <- gM.V) {
      val relM = findRelations(v, gM)
      val relV = findRelations(v, gVirtual)

      if ((relV -- relM).nonEmpty && relV.intersect(renaming.keySet).isEmpty) {
        val externalNames = gVirtual.EOut.values.flatten.toSet
        val propagatedNames = relV.intersect(externalNames).map(_.name)
        if (propagatedNames.size == 1) {
          renaming ++= (relV -- externalNames).map(r => (r, propagatedNames.head))
        }
        else
          sys.error("Unable to retain relations to external identifiers with different names!")
      }
    }

    m.rename(renaming)
  }

  def nameFixModule[S <: Meta, T <: NominalModular[S]](gS: NameGraphModular, mT: T, metaDep: Set[S]) = {
    val gVirtual = nameFixVirtual(gS, mT, metaDep, metaDep.flatMap(_.export.map(id => (id, id.originalName))).toMap)
    applyVirtualGraph(mT, metaDep, gVirtual)
  }

  def nameFixModules[S <: Meta, T <: NominalModular[S]](mS: Set[T], metaS: Set[S], mT: Set[T], metaT: Set[S]): Set[T] = {
    if (mT.isEmpty)
      return Set()

    val currentModuleT = mT.find(m => m.dependencies.forall(i => metaT.exists(_.moduleID.name == i))).get
    val currentModuleS = mS.find(_.moduleID == currentModuleT.moduleID)
    val (currentGS, currentMetaS) = currentModuleS match {
      case Some(module) => module.resolveNamesModular(metaS)
      case None => (NameGraphModular(Set(), Map(), Map()), null)
    }

    val (currentModuleFixed, metaFixed) = nameFixModule(currentGS, currentModuleT, metaT)
    val newMetaS = if (currentMetaS != null) metaS + currentMetaS.asInstanceOf[S] else metaS

    nameFixModules(mS, newMetaS, mT - currentModuleT, metaT + metaFixed) + currentModuleFixed.asInstanceOf[T]
   }
 }