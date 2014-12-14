package name

import name.Gensym._

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

  private def compRenamings(gs: NameGraph, t: Nominal, nodesToRename: Set[Name.ID]): Map[Name.ID, String] = {
    var renaming: Map[Name.ID, String] = Map()
    val newIds = t.allNames -- gs.V.map(_._1)

    for (v <- nodesToRename) {
      val fresh = gensym(v.name, t.allNames.map(_.name) ++ renaming.values)
      if (gs.V.exists(_._1 == v)) {
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

  protected def nameFixCaptures[T <: Nominal](gs: NameGraph, t: T): (T, Map[Name.ID, String]) = {
    val gt = t.resolveNames
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

  protected def nameFixErrors[T <: Nominal](gs: NameGraph, t: T) : (T, Map[Name.ID, String]) = {
    val gt = t.resolveNames
    var renamingNodes: Set[Name.ID] = Set()

    for (multipleDeclarationNodes <- gt.C) {
      val synthesizedNodes = multipleDeclarationNodes.filterNot(v => gs.V.exists(_._1 == v))
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

    // Currently only a warning message, later additional handling
    if (t.resolveNames.V.exists(v => (renaming ++ renamingError).contains(v._1) && v._2))
      println("NameFix failed to find a fix without renaming exported names!")

    tFixedFinal
  }
}

class NameFixModular extends NameFix {
  override def nameFix[T <: Nominal](gs: NameGraph, t: T): T = {
    // Is this a necessary restriction?
    if (gs.C.size != 0) sys.error("NameFix can't fix names for a source name graph with errors!")

    // Step 1: Classic NameFix for captures
    val (tFixed1, renaming) = nameFixCaptures(gs, t)

    // Step 2: Fix name graph errors
    val (tFixed2, renamingError) = nameFixErrors(gs, tFixed1)

    // Step 2: Find alternative renamings for exported names based on the fixed graph
    val (tFixedFinal, exportedNameRenamed) = nameFixFindAlternatives(t, tFixed2, renaming ++ renamingError)

    // Currently only a warning message, later additional handling
    if (exportedNameRenamed)
      println("NameFix failed to find a fix without renaming exported names!")

    tFixedFinal
  }

  private def nameFixFindAlternatives[T <: Nominal](t : T, tFixed : T, renaming : Map[Name.ID, String]) : (T, Boolean) = {
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

    // If no renaming was done, the given graph is equivalent to the fixed one and we are finished
    if (alternativeRenaming.isEmpty) {
      (t, false)
    }
    else {
      // Else, apply the calculated alternative renaming ...
      val tFixedAlternative = t.rename(alternativeRenaming).asInstanceOf[T]
      // ... and perform a recursive call with the alternative result
      val (tFinal, exportedNameRenamedRecursive) = nameFixFindAlternatives(tFixedAlternative, tFixed, renaming ++ alternativeRenaming)

      (tFinal, exportedNameRenamed || exportedNameRenamedRecursive)
    }

  }
}