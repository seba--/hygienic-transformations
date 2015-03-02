package name.namefix

import name.Gensym._
import name._
import name.namegraph.NameGraphExtended
import name.namegraph.NameGraphExtended.Nodes

class NameFixExtended {
  protected def findRelations(n: Identifier,g: NameGraphExtended, result: Nodes = Set()): Nodes = {
    var newResult = result + n

    if (g.E.contains(n))
        newResult ++= (g.E(n) -- newResult).flatMap(d => findRelations(d, g, newResult))
    for ((v, d) <- g.E if !newResult.contains(v) && d.contains(n))
        newResult ++= findRelations(v, g, newResult)

    newResult
  }

  protected def findCapturedNodes(gs: NameGraphExtended, gt: NameGraphExtended): Nodes = {
    gt.V.filter(v => gs.V.contains(v) && (findRelations(v, gt) -- findRelations(v, gs)).nonEmpty)
  }

  protected def compRenamings(gs: NameGraphExtended, gt: NameGraphExtended, t: Nominal, toRename: Nodes): Map[Identifier, Name] = {
    var renaming: Map[Identifier, Name] = Map()

    for (v <- toRename if findRelations(v, gt).intersect(renaming.keySet).isEmpty) {
      val fresh = gensym(v.name, t.allNames.map(_.name) ++ renaming.values)
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