package ref

import name.GensymPure._
import name._
import name.namegraph.NameGraph.{Edges, Nodes}
import name.namegraph.{NameGraph, NameGraphExtended, NameGraphModular}

object RefFix {
  // Global NameFix (as presented in ecoop14-paper)
  def refFix[T <: Structural](gs: RefGraph, t: T, permittedCapture: Set[Reference] = Set()) =
    new RefFix(permittedCapture).refFix(gs, t)
}

class RefFix(permittedCapture: Set[Reference] = Set()) {

  def findCapturedNodes(gs: RefGraph, gt: RefGraph): Set[Reference] = {

    // newrefs whose target was changed
    val notPreservedRefs = gt.refs.filter { case newref =>
      gs.refs.exists(oldref => oldref == newref && oldref.target != newref.target)
    }

    // synthesized newrefs pointing to original decls
    val notPreserveDef = gt.refs.filter { case newref =>
      gs.decls.contains(newref.target) && !gs.refs.contains(newref)
    }

    notPreservedRefs ++ notPreserveDef
  }

  def compRetargeting(gs: RefGraph, gt: RefGraph, t: Structural, capture: Set[Reference]): Map[Reference, Declaration] = {
    var retargeting: Map[Reference, Declaration] = Map()

    val captureDecls = capture.map(_.target)

    for (d <- captureDecls) {
      for (r <- gs.refs if d == r.target)
        retargeting += (r -> d)
    }

    retargeting
  }

  def refFix[T <: Structural](gs: RefGraph, t: T): T = {
    val gt = t.resolveRefs
    val capture = findCapturedNodes(gs, gt) -- permittedCapture
    if (capture.isEmpty)
      t
    else {
      val retargeting = compRetargeting(gs, gt, t, capture)
      val tNew = t.retarget(retargeting).asInstanceOf[T]
      refFix(gs, tNew)
    }
  }
}