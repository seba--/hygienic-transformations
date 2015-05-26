package name

import Name._
import NameGraph._
import Gensym._

/**
 * Created by seba on 01/08/14.
 */
object NameFix {
  val fixer = new NameFix
  def nameFix[T <: Nominal](gs: NameGraph, t: T, permittedCapture: Edges = Map()): T = fixer.nameFix(gs, t, permittedCapture)
}

class NameFix {
  def findCapture(gs: NameGraph, Et: NameGraph.Edges): Edges = {

    val notPreserveVar = Et.filter {
      case (v,d) => gs.V.contains(v) && (gs.E.get(v) match {
        case Some(ds) => d != ds
        case None => v != d
      })
    }

    val notPreserveDef = Et.filter {
      case (v,d) => !gs.V.contains(v) && gs.V.contains(d)
    }

    notPreserveVar ++ notPreserveDef
  }

  def compRenamings(gs: NameGraph, t: Nominal, capture: Edges): Map[Name.ID, String] = {
    var renaming: Map[Name.ID, String] = Map()
    val newIds = t.allNames -- gs.V

    for (d <- capture.values) {
      val fresh = gensym(d.name, t.allNames.map(_.name) ++ renaming.values)
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

  def nameFix[T <: Nominal](gs: NameGraph, t: T, permittedCapture: Edges = Map()): T = {
    val gt = t.resolveNames
    val allcapture = findCapture(gs, gt.E)
    val capture = allcapture filter (kv => permittedCapture.getOrElse(kv._1, null) != kv._2)
    if (capture.isEmpty)
      t
    else {
      val renaming = compRenamings(gs, t, capture)
      val tnew = t.rename(renaming).asInstanceOf[T]
      nameFix(gs, tnew, permittedCapture)
    }
  }
}
