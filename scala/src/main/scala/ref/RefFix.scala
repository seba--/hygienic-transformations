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
      gs.refs.exists(oldref => oldref == newref && ((oldref.hasTarget, newref.hasTarget) match {
        case (true, true) => oldref.target != newref.target // was bound, now bound: not ok if target changed
        case (false, true) => true // was unbound, now bound: not ok
        case (true, false) => false // was bound, now unbound: ok
        case (false, false) => false // was unbound, now unbound: ok
      }))
    }

    // synthesized newrefs pointing to original decls
    val notPreserveDef = gt.refs.filter { case newref =>
      newref.hasTarget && gs.decls.contains(newref.target) && !gs.refs.contains(newref)
    }

    notPreservedRefs ++ notPreserveDef
  }

  def compRetargeting(gs: RefGraph, gt: RefGraph, t: Structural, capture: Set[Reference]): Map[Reference, Option[Declaration]] = {
    var retargeting: Map[Reference, Option[Declaration]] = Map()

    for (r <- capture)
      if (gs.refs.contains(r)) {
        // existing refs:
        val old = gs.refs.find(_ == r).get
        val newtarget = if (old.hasTarget) Some(old.target) else None
        retargeting += (r -> newtarget)
      }
      else {
        // synthesized refs:
        // we cannot infer what the ref is supposed to point to
        // possible solution: the transformation has to explicitly set targets for synthesized refs
        retargeting += (r -> None)
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
      tNew
    }
  }
}