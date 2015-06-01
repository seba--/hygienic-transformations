package name.namefix

import name.Gensym._
import name._
import name.namegraph.NameGraphModular
import name.namegraph.NameGraphModular.Nodes

class NameFixModular[I <: NameInterface] {
  protected def findRelations(n: Identifier,g: NameGraphModular[I], result: Nodes = Set()): Nodes = {
    var newResult = result + n

    if (g.E.contains(n))
        newResult ++= (g.E(n) -- newResult).flatMap(d => findRelations(d, g, newResult))
    for ((v, d) <- g.E if !newResult.contains(v) && d.contains(n))
        newResult ++= findRelations(v, g, newResult)

    newResult
  }

  protected def findCaptureNodes(gs: NameGraphModular[I], gt: NameGraphModular[I]) = {
    val gsNames = gs.V ++ gs.IUsed.flatMap(_.export)
    val gtNames = gt.V ++ gt.IUsed.flatMap(_.export)

    gsNames.intersect(gtNames).filter(v => (findRelations(v, gt) -- findRelations(v, gs)).nonEmpty)
  }

  protected def compRenamings(gs: NameGraphModular[I], gt: NameGraphModular[I], t: NominalModular[I], captureNodes: Nodes) = {
    var renaming: Map[Identifier, Name] = Map()

    for (v <- captureNodes if findRelations(v, gt).intersect(renaming.keySet).isEmpty) {
      val fresh = gensym(v.name, t.allNames ++ gt.IUsed.flatMap(_.export.map(_.name)) ++ renaming.values)
      val relatedNames = findRelations(v, gs)
      renaming ++= relatedNames.map(r => (r, fresh))
    }

    renaming
  }

  protected def nameFixVirtual(gS: NameGraphModular[I], mT: NominalModular[I], depT: Set[I]): NameGraphModular[I] = {
    val gT = mT.link(depT).resolveNamesModular
    val capture = findCaptureNodes(gS, gT)

    if (capture.isEmpty)
      gT
    else {
      val renamings = compRenamings(gS, gT, mT, capture)
      val mTNew = mT.rename(renamings)
      val depTNew = depT.map(_.rename(renamings).asInstanceOf[I])
      nameFixVirtual(gS, mTNew, depTNew)
    }
  }

  protected def selectRenaming(rel1: Nodes, rel2: Nodes, fresh: Name, g: NameGraphModular[I]) = {
    val externalNames = g.IUsed.flatMap(_.export)

    if ((rel1 ++ rel2).intersect(externalNames).isEmpty) {
      if (rel1.intersect(g.I.export).size > rel2.intersect(g.I.export).size)
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
        throw new IllegalArgumentException("Unable to fix module without renaming external identifiers!")
    }
  }

  protected def removeUnintendedRelations[T <: NominalModular[I]](mT: T, gVirtual: NameGraphModular[I]): T = {
    var renaming: Map[Identifier, Name] = Map()
    val gT = mT.resolveNamesModular

    for (v <- gT.V) {
      val fresh = gensym(v.name, mT.allNames ++ gT.IUsed.flatMap(_.export.map(_.name)) ++ renaming.values)

      val relM = findRelations(v, gT)
      val relV = findRelations(v, gVirtual)
      val relRemove = (relM -- relV).flatMap(r => findRelations(r, gVirtual))

      if (relRemove.nonEmpty && (relRemove ++ relV).intersect(renaming.keySet).isEmpty)
        renaming ++= selectRenaming(relV, relRemove, fresh, gVirtual)
    }

    if (renaming.isEmpty)
      mT
    else {
      val mNew = mT.rename(renaming).asInstanceOf[T]
      removeUnintendedRelations(mNew, gVirtual)
    }
  }

  protected def addIntendedRelations[T <: NominalModular[I]](mT: T, gVirtual: NameGraphModular[I]): T = {
    var renaming: Map[Identifier, Name] = Map()
    val gT = mT.resolveNamesModular

    for (v <- gT.V) {
      val relT = findRelations(v, gT)
      val relV = findRelations(v, gVirtual)

      val lostBindings = relV -- relT

      if (lostBindings.nonEmpty && relV.intersect(renaming.keySet).isEmpty) {
        val intendedBindings = relV.intersect(gVirtual.IUsed.flatMap(_.export)).map(_.name)
        if (intendedBindings.size == 1) {
          renaming ++= relV.intersect(gT.V).map(r => r -> intendedBindings.head)
        }
        else
          throw new IllegalArgumentException("Unable to retain relations to external identifiers with different names!")
      }
    }

    mT.rename(renaming).asInstanceOf[T]
  }

  protected def applyVirtualGraph[T <: NominalModular[I]](mT: T, gVirtual: NameGraphModular[I]): T = {
    val mTNew = addIntendedRelations(mT, gVirtual)
    removeUnintendedRelations(mTNew, gVirtual)
  }

  def nameFixModule[T <: NominalModular[I]](gS: NameGraphModular[I], mT: T, depT: Set[I]): T = {
    val gVirtual = nameFixVirtual(gS, mT, depT.map(_.original.asInstanceOf[I]))
    val linked = mT.link(depT).asInstanceOf[T]
    applyVirtualGraph(linked, gVirtual)
  }

//  def nameFixModules[T <: NominalModular[I]](gS: Set[NameGraphModular[I]],  mT: Set[T], depT: Set[I]): Set[T] = {
//    if (mT.isEmpty)
//      Set()
//    else {
//      val currentModuleT = mT.find(m => m.dependencies.forall(i => depT.exists(_.moduleID == i))) match {
//        case Some(module) => module
//        case None => throw new IllegalArgumentException("Unable to resolve these modules based on their dependencies: " + mT.map(_.moduleID).mkString(", "))
//      }
//      val currentGS = gS.find(_.I.moduleID == currentModuleT.moduleID).getOrElse {
//        throw new IllegalArgumentException("No source name graph found for module " + currentModuleT.moduleID)
//      }
//
//      val currentModuleFixed = nameFixModule(currentGS, currentModuleT, depT)
//      val currentNameGraphFixed = currentModuleFixed.resolveNamesModular
//
//      nameFixModules(gS, mT - currentModuleT, depT + currentNameGraphFixed.I) + currentModuleFixed
//    }
//  }
}