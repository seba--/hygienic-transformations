package name.namefix

import name.Gensym._
import name._
import name.namegraph.NameGraph.{Edges, Nodes}
import name.namegraph.{NameGraphModular, NameGraphExtended, NameGraph}

/**
 * Created by seba on 01/08/14.
 */
object NameFix {
  // Global NameFix (as presented in ecoop14-paper)
  def nameFix[T <: Nominal](gs: NameGraph, t: T, permittedCapture: Edges = Map()) = new NameFix(permittedCapture).nameFix(gs, t)

  // Extended Global NameFix, using relations instead of references and as a result supports transitivity and multiple outgoing edges per ID
  def nameFixExtended[T <: Nominal](gs: NameGraphExtended, t: T, permittedCapture: NameGraphExtended.Edges = Map()) = new NameFixExtended(permittedCapture).nameFix(gs, t)

  // Modular NameFix applied on a single module + dependencies
  def nameFix[I <: NameInterface, T <: NominalModular[I]](gs: NameGraphModular[I], t: T, depT: Set[I]) = new NameFixModular[I].nameFixModule(gs, t, depT)

  // Modular NameFix applied on a set of modules + dependencies
  def nameFix[I <: NameInterface, T <: NominalModular[I]](gs: Set[NameGraphModular[I]], mT: Set[T], depT: Set[I]) = new NameFixModular[I].nameFixModules(gs, mT, depT)
}

class NameFix(permittedCapture: Edges = Map()) {
  def findCapturedNodes(gs: NameGraph, gt: NameGraph): Nodes = {

    val notPreserveVar = gt.E.filter {
      case (v,d) => gs.V.contains(v) && (gs.E.get(v) match {
        case Some(ds) => d != ds
        case None => v != d
      })
    }

    val notPreserveDef = gt.E.filter {
      case (v,d) => !gs.V.contains(v) && gs.V.contains(d)
    }

    (notPreserveVar ++ notPreserveDef).values.toSet
  }

  def compRenamings(gs: NameGraph, gt: NameGraph, t: Nominal, capture: Nodes): Map[Identifier, Name] = {
    var renaming: Map[Identifier, Name] = Map()
    val newIds = gt.V -- gs.V

    for (d <- capture) {
      val fresh = gensym(d.name, t.allNames ++ renaming.values)
      if (gs.V.contains(d)) {
        renaming += (d -> fresh)
        for ((v2,d2) <- gs.E if d == d2)
          renaming += (v2 -> fresh)
      }
      else {
        for (v2 <- newIds if d.name == v2.name)
          renaming += (v2 -> fresh)
      }
    }

    renaming
  }

  def nameFix[T <: Nominal](gs: NameGraph, t: T): T = {
    val gt = t.resolveNames.toSimple
    val capture = findCapturedNodes(gs, gt)
    if (capture.isEmpty)
      t
    else {
      val renaming = compRenamings(gs, gt, t, capture)
      val tNew = t.rename(renaming).asInstanceOf[T]
      nameFix(gs, tNew)
    }
  }
}