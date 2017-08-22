package name.namefix

import name.GensymPure._
import name._
import name.namegraph.NameGraphExtended
import name.namegraph.NameGraphExtended.{Nodes,Edges}

class NameFixExtended(permittedCapture: Edges = Map()) {

  def ignoreEdge(ref: Identifier, dec: Identifier) = permittedCapture.get(ref) match {
    case None => false
    case Some(decs) => decs.contains(dec)
  }

  protected def findRelations(n: Identifier, g: NameGraphExtended, result: Nodes = Set()): Nodes = {
    var newResult = result + n

    g.E.get(n) match {
      case Some(decs) =>
        val relevantDecs = decs -- permittedCapture.getOrElse(n, Set())
        newResult ++= (relevantDecs -- newResult).flatMap(dec => findRelations(dec, g, newResult))
      case None =>
    }

    for ((v, d) <- g.E if !newResult.contains(v) && d.contains(n) && !ignoreEdge(v, n))
        newResult ++= findRelations(v, g, newResult)

    newResult
  }

  def isCaptured(v: Identifier, gs: NameGraphExtended, gt: NameGraphExtended) = {
    val trel = findRelations(v, gt)
    val srel = findRelations(v, gs)
    val diff = trel -- srel
    diff.nonEmpty
  }

  protected def findCapturedNodes(gs: NameGraphExtended, gt: NameGraphExtended): Nodes = {
    gt.V.filter(v => gs.V.contains(v) && isCaptured(v, gs, gt))
  }

  protected def compRenamings(gs: NameGraphExtended, gt: NameGraphExtended, t: Nominal, toRename: Nodes): Map[Identifier, Name] = {
    var renaming: Map[Identifier, Name] = Map()

    for (v <- toRename if findRelations(v, gt).intersect(renaming.keySet).isEmpty) {
      val fresh = gensym(v.name, t.allNames ++ renaming.values)
      val relatedNames = findRelations(v, gs)
      renaming ++= relatedNames.map(r => (r, fresh))
    }

    renaming
  }

  def nameFix[T <: Nominal](gs: NameGraphExtended, t: T): T = {
    val gt = t.resolveNames
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