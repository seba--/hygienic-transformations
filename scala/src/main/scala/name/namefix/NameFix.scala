package name.namefix

import name.Gensym._
import name._

/**
 * Created by seba on 01/08/14.
 */
object NameFix {
  val fixer = new NameFix
  val fixerModular = new NameFixModular

  def nameFix[T <: Nominal, U <: NameGraph](gs: U, t: T): T = (gs, t) match {
    case (NameGraphGlobal(_, _, _), _)  => fixer.nameFix(gs, t)
    case (NameGraphModular(_, _, _, _, _), tm:NominalModular) => fixerModular.nameFix(gs, t)
  }
}

class NameFix {
  private def findCapture(gs: NameGraph, gt: NameGraph): Edges = {
    val notPreserveVar = gt.E.filter {
      case (v,d) => gs.V.contains(v) && (gs.E.get(v) match {
        case Some(ds) => d != ds
        case None => v != d
      })
    }
    val notPreserveDef = gt.E.filter {
      case (v,d) => !gs.V.contains(v) && gs.V.contains(d)
    }

    notPreserveVar ++ notPreserveDef
  }

  private def compRenamings(gs: NameGraph, t: Nominal, nodesToRename: Set[Name.ID]): Map[Name.ID, String] = {
    var renaming: Map[Name.ID, String] = Map()
    val newIds = t.allNames -- gs.V

    for (v <- nodesToRename) {
      val fresh = gensym(v.name, t.allNames.map(_.name) ++ renaming.values)
      if (gs.V.contains(v)) {
        renaming += (v -> fresh)
        for (v2 <- findConnectedNodes(gs, v))
          renaming += (v2 -> fresh)
      }
      else {
        for (v2 <- newIds if v.name == v2.name)
          renaming += (v2 -> fresh)
      }
    }

    renaming
  }

  protected def findConnectedNodes(g: NameGraph, n: Name.ID, result: Set[Name.ID] = Set()): Set[Name.ID] = {
    // Handling this here to simplify final renaming method
    if (n == null) Set()

    var newResult = result + n

    for ((v, d) <- g.E if d == n)
      if (!newResult.contains(v))
        newResult ++= findConnectedNodes(g, v, newResult)
    for ((v, d) <- g.E if v == n)
      if (!newResult.contains(d))
        newResult ++= findConnectedNodes(g, d, newResult)

    newResult
  }

  private def nameFixCaptures[T <: Nominal](gs: NameGraph, t: T): (T, Map[Name.ID, String]) = {
    val gt = t.resolveNames()
    val capture = findCapture(gs, gt)
    if (capture.isEmpty)
      (t, Map())
    else {
      val renaming = compRenamings(gs, t, capture.values.toSet)

      val tNew = t.rename(renaming).asInstanceOf[T]

      val (tNameFixed, recursiveRenaming) = nameFixCaptures(gs, tNew)

      (tNameFixed, renaming ++ recursiveRenaming)
    }
  }

  private def nameFixErrors[T <: Nominal](gs: NameGraph, t: T) : (T, Map[Name.ID, String]) = {
    val gt = t.resolveNames()
    var renamingNodes: Set[Name.ID] = Set()

    for (multipleDeclarationNodes <- gt.C) {
      val synthesizedNodes = multipleDeclarationNodes.filterNot(v => gs.V.contains(v))
      if (synthesizedNodes.nonEmpty) {
        renamingNodes ++= synthesizedNodes
      } else {
        // Get all nodes that were not contained in any multiple declaration error of the source graph
        val newErrorNodes = multipleDeclarationNodes.filter(v => !gs.C.exists(_.contains(v)))
        renamingNodes ++= newErrorNodes
      }
    }

    if (renamingNodes.isEmpty) {
      (t, Map())
    }
    else {
      val renaming = compRenamings(gs, t, renamingNodes)

      // Apply the calculated renaming
      val tFixed = t.rename(renaming).asInstanceOf[T]

      val (tFinal, recursiveRenaming) = nameFixErrors(gs, tFixed)

      (tFinal, renaming ++ recursiveRenaming)
    }
  }

  def nameFix[T <: Nominal](gs: NameGraph, t: T): T = {
    // Step 1: Classic NameFix for captures
    val (tFixed, renaming) = nameFixCaptures(gs, t)

    // Step 2: Fix name graph errors
    val (tFixedFinal, renamingError) = nameFixErrors(gs, tFixed)

    tFixedFinal
  }
}