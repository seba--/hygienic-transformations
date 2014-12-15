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

  private def nameFixErrors[T <: Nominal](gs: NameGraph, t: T) : (T, Map[Name.ID, String]) = {
    val gt = t.resolveNames
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

class NameFixModular extends NameFix {
  private def findCapture(gs: NameGraphModular, gt: NameGraphModular, fixedModules : Set[Meta]): (Edges, OutEdges) = {
    val notPreserveVarLocal = gt.E.filter {
      case (v,d) => gs.V.contains(v) && (gs.E.get(v) match {
        case Some(ds) => d != ds
        case None => v != d
      })
    }
    val notPreserveDefLocal = gt.E.filter {
      case (v,d) => !gs.V.contains(v) && gs.V.contains(d)
    }

    val notPreserveVarExternal = gt.EOut.filter {
      case (v,d) => gs.V.contains(v) && (gs.EOut.get(v) match {
        case Some(ds) => d != ds
        case None => true
      })
    }
    val notPreserveDefExternal = gt.EOut.filter {
      case (v,d) => !gs.V.contains(v) && fixedModules.exists(meta => meta._1 == d._1 && meta._2.exists(_.id == d._2))
    }

    (notPreserveVarLocal ++ notPreserveDefLocal, notPreserveVarExternal ++ notPreserveDefExternal)
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

  private def compDependencyRenamings(nodesToRename: Set[Name.ID], allNames : Set[String]): Map[Name.ID, String] = {
    var renaming: Map[Name.ID, String] = Map()

    for (v <- nodesToRename) {
      val fresh = gensym(v.name, allNames ++ renaming.values)
      renaming += (v -> fresh)
    }

    renaming
  }

  private def nameFixCaptures[T <: NominalModular](gs: NameGraphModular, t: T, renamedDependencies : Renaming,
                                                     fixedModules : Set[Meta]) : (T, Map[Name.ID, String]) = {
    val gt = t.resolveNames(renamedDependencies)
    val capture = findCapture(gs, gt, fixedModules)
    if (capture._1.isEmpty && capture._2.isEmpty)
      (t, Map())
    else {
      val renaming = compRenamings(gs, t, capture._1.values.toSet)
      val allNames = t.allNames.map(_.name) ++ fixedModules.flatMap(meta => meta._2 ++ meta._3).map(_.name)
      val virtuallyRenamedDependencies = compDependencyRenamings(capture._2.values.map(_._2).toSet, allNames)

      val tNew = t.rename(renaming).asInstanceOf[T]

      val (tNameFixed, recursiveRenaming) = nameFixCaptures(gs, tNew, renamedDependencies ++ virtuallyRenamedDependencies, fixedModules)

      (tNameFixed, renaming ++ virtuallyRenamedDependencies ++ recursiveRenaming)
    }
  }

  protected def nameFixModule[T <: NominalModular](gs: NameGraphModular, t: T, fixedModules : Set[Meta]): (T, Meta) = {
    // Step 1: Classic NameFix for captures
    val (tFixed1, renaming) = nameFixCaptures(gs, t, fixedModules.flatMap(_._4).toMap, fixedModules)

    // Step 2: Fix name graph errors
    //val (tFixed2, renamingError) = nameFixErrors(gs, tFixed1)

    // Step 2: Find alternative renamings for exported names based on the fixed graph
    val (tFixedFinal, exportedNameRenamed) = nameFixFindAlternatives(t, tFixed1, renaming, tFixed1.exportedNames)

    // Currently only a warning message, later additional handling
    if (exportedNameRenamed)
      println("NameFix failed to find a fix without renaming exported names!")

    val id = gs match {
      case NameGraphModular(ngID, _, _, _, _) => ngID
      case _ => null
    }
    val exportedNames = t match {
      case nm:NominalModular => nm.exportedNames
      case _ => Set[Name]()
    }

    (tFixedFinal, (id, exportedNames.filter(n => gs.V.contains(n.id)), exportedNames, renaming))
  }

  def nameFix[T <: NominalModular](modules : Set[(NameGraphModular, T)], fixedModules : Set[(T, Meta)]): Set[(T, Meta)] = {
    val fixableModules = modules.filter(_._1.EOut.forall(referredGraph => !modules.exists(_._1.ID == referredGraph._2._1)))
    if (fixableModules.isEmpty)
      sys.error("Encountered cyclic dependency of input modules! Cycles aren't supported by NameFix!")
    else {
      val currentModule = fixableModules.head
      val currentModuleFixed = nameFixModule(currentModule._1, currentModule._2, fixedModules.map(_._2))
      if ((modules - currentModule).isEmpty)
        fixedModules + currentModuleFixed
      else
        nameFix(modules - currentModule, fixedModules + currentModuleFixed)
    }
  }

  private def nameFixFindAlternatives[T <: Nominal](t : T, tFixed : T, renaming : Map[Name.ID, String], exportedNames : Set[Name]) : (T, Boolean) = {
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
        if (gTFixed.V.exists(v2 => dOldReferences.contains(v2) && exportedNames.exists(_.id == v2))) {

          // Get all nodes connected to the source node in the fixed graph
          // Note that this leads to the same result as using dNew if there is a new edge, but if there isn't, v itself is also a valid renaming option.
          val dNewReferences = findConnectedNodes(gTFixed, v)
          // Check if none of them is exported
          if (!gTFixed.V.exists(v2 => dNewReferences.contains(v2)  && exportedNames.exists(_.id == v2))) {
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
      val (tFinal, exportedNameRenamedRecursive) = nameFixFindAlternatives(tFixedAlternative, tFixed, renaming ++ alternativeRenaming, exportedNames)

      (tFinal, exportedNameRenamed || exportedNameRenamedRecursive)
    }

  }
}