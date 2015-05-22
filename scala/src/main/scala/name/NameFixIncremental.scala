package name

import Name._
import NameGraph._
import Gensym._

/**
 * WORK IN PROGRESS
 */
class NameFixIncremental extends NameFix {

  override def nameFix[T <: Nominal](gs: NameGraph, t: T): T = nameFixIncremental(gs, t, t.resolveNames.E)

  /* gtDiff: added nodes, added edges, updated edges */
  def nameFixIncremental[T <: Nominal](gs: NameGraph, t: T, gtDiff: NameGraph.Edges): T = {
    val capture = findCapture(gs, gtDiff)

    if (capture.isEmpty)
      t
    else {
      val renaming = compRenamings(gs, t, capture)
      val tnew = t.rename(renaming).asInstanceOf[T]

      val gtNew = tnew.resolveNames
      val gRenamedDiff = gtNew.E filter (kv => renaming.contains(kv._1) || renaming.contains(kv._2))
      nameFixIncremental(gs, tnew, gRenamedDiff)
    }
  }
}
