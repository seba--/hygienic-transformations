package name

import name.Gensym._
import name.NameGraph._

/**
 * Created by seba on 01/08/14.
 */
object NameFix {
  val fixer = new NameFix
  def nameFix[T <: Nominal](gs: NameGraph, t: T): T = fixer.nameFix(gs, t)
}

class NameFix {

  def findCapture(gs: NameGraph, gt: NameGraph): Edges = {
    val notPreserveVar = gt.E.filter {
      case (v,d) => gs.V.exists(_._1 == v) && (gs.E.get(v) match {
        case Some(ds) => d != ds
        case None => v != d
      })
    }
    val notPreserveDef = gt.E.filter {
      case (v,d) => !gs.V.exists(_._1 == v) && gs.V.exists(_._1 == d)
    }

    notPreserveVar ++ notPreserveDef
  }

  def compRenamings(gs: NameGraph, t: Nominal, capture: Edges): Map[Name.ID, String] = {
    var renaming: Map[Name.ID, String] = Map()
    val newIds = t.allNames -- gs.V.map(_._1)

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

  def findConnectedNodes(g: NameGraph, n: Name.ID, result: Set[Name.ID] = Set()): Set[Name.ID] = {
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

  def nameFix[T <: Nominal](gs: NameGraph, t: T): T = {
    // Is this a necessary restriction?
    if (gs.Err.size != 0) sys.error("NameFix can't fix names for a source name graph with errors!")

    // Step 1: Classic NameFix for captures
    val (tFixed1, renaming) = nameFixCaptures(gs, t)

    // Step 2: Find alternative renamings for exported names based on the fixed graph
    val (tFixed2, exportedNameRenamed1) = nameFixFindAlternatives(t, tFixed1, renaming)

    // Step 3: Fix name graph errors
    val (tFixedFinal, exportedNameRenamed2) = nameFixErrors(gs, tFixed2)

    // Currently only a warning message, later additional handling
    if (exportedNameRenamed1 || exportedNameRenamed2)
      println("NameFix failed to find a fix without renaming exported names!")

    tFixedFinal
  }

  def nameFixCaptures[T <: Nominal](gs: NameGraph, t: T): (T, Map[Name.ID, String]) = {
    val gt = t.resolveNames
    val capture = findCapture(gs, gt)
    if (capture.isEmpty)
      (t, Map())
    else {
      val renaming = compRenamings(gs, t, capture)

      val tNew = t.rename(renaming).asInstanceOf[T]

      val (tNameFixed, recursiveRenaming) = nameFixCaptures(gs, tNew)

      (tNameFixed, renaming ++ recursiveRenaming)
    }
  }

  def nameFixErrors[T <: Nominal](gs: NameGraph, t: T) = {
    val gt = t.resolveNames
    var renaming: Map[Name.ID, String] = Map()
    var exportedNameRenamed = false

    for (error <- gt.Err) {
      error match {
        case MultipleDeclarationsError(errorNodes) =>
          // Find all nodes related to the error that are not exported
          val notExportedNodes = errorNodes.filter(node => gt.V.contains((node, false)))
          // As all nodes but one need to be renamed, two or more exported nodes force renaming of an exported node
          if (errorNodes.size - notExportedNodes.size > 1)
            exportedNameRenamed = true
          // If exported node renaming can be avoided, select all non-exported nodes for renaming, else select all of them
          val nodesToRename = if (errorNodes.size - notExportedNodes.size > 1) errorNodes else notExportedNodes

          for (nodeToRename <- nodesToRename) {
            // Generate a fresh name for each node to rename and add it to the renaming list
            val fresh = Gensym.gensym(nodeToRename.name, t.allNames.map(_.name) ++ renaming.values)
            for (n <- findConnectedNodes(gt, nodeToRename))
            // Re-use the existing name ID!
              renaming += (n -> fresh)
          }
      }
    }

    // Apply the calculated renaming
    val tFixed = t.rename(renaming).asInstanceOf[T]

    (tFixed, exportedNameRenamed)
  }

  def nameFixFindAlternatives[T <: Nominal](t : T, tFixed : T, renaming : Map[Name.ID, String]) = {
    val gT = t.resolveNames
    val gTFixed = tFixed.resolveNames

    var exportedNameRenamed = false
    var alternativeRenaming : Map[Name.ID, String] = Map()

    // For each edge in the unfixed graph ...
    for ((v, dOld) <- gT.E) {
      // ... get the edge with the same source node in the fixed graph (there might also be no edge any more!)
      val dNew = gTFixed.E.getOrElse(v, null)

      // Only need to handle edges that changed through fixing
      if (dOld != dNew) {
        // Get all nodes connected to the old target node in the fixed graph
        val dOldReferences = findConnectedNodes(gTFixed, dOld)
        // Check if any of them is exported
        if (gTFixed.V.exists(v2 => dOldReferences.contains(v2._1) && v2._2)) {

          // Get all nodes connected to the source node in the fixed graph
          // Note that this leads to the same result as using dNew if there is a new edge, but if there isn't, v itself is also a valid renaming option.
          val dNewReferences = findConnectedNodes(gTFixed, v)
          // Check if none of them is exported
          if (!gTFixed.V.exists(v2 => dNewReferences.contains(v2._1) && v2._2)) {
            // Generate an all new name for the alternative renaming
            val fresh = Gensym.gensym(v.name, tFixed.allNames.map(_.name) ++ renaming.values ++ alternativeRenaming.values)
            alternativeRenaming ++= dNewReferences.map(r => (r, fresh)).toMap[Name.ID, String]
          }
          // If both nodes have connected nodes that are exported, renaming one of them is unavoidable!
          else {
            exportedNameRenamed = true
            // Choosing the renaming found by original name fix
            alternativeRenaming ++= dOldReferences.map(r => (r, renaming.getOrElse(r, r.name))).toMap[Name.ID, String]
          }

        }
        // If no node is exported, the renaming found by original name fix is sufficient and can be re-used
        else {
          alternativeRenaming ++= dOldReferences.map(r => (r, renaming.getOrElse(r, r.name))).toMap[Name.ID, String]
        }
      }
    }

    // Apply the calculated alternative renaming
    val tFixedAlternative = t.rename(alternativeRenaming).asInstanceOf[T]

    (tFixedAlternative, exportedNameRenamed)
  }
}