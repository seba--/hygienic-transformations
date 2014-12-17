package name.namefix

import name.Gensym._
import name._

class NameFixModular extends NameFix {
  private def findCapture(gs: NameGraphModular, gt: NameGraphModular, fixedModules: Set[Meta]): (Edges, OutEdges) = {
    val notPreserveVarLocal = gt.E.filter {
      case (v, d) => gs.V.contains(v) && (gs.E.get(v) match {
        case Some(ds) => d != ds
        case None => v != d
      })
    }
    val notPreserveDefLocal = gt.E.filter {
      case (v, d) => !gs.V.contains(v) && gs.V.contains(d)
    }

    val notPreserveVarExternal = gt.EOut.filter {
      case (v, d) => gs.V.contains(v) && (gs.EOut.get(v) match {
        case Some(ds) => d != ds
        case None => true
      })
    }
    val notPreserveDefExternal = gt.EOut.filter {
      case (v, d) => !gs.V.contains(v) && fixedModules.exists(meta => meta._1 == d._1 && meta._2.exists(_.id == d._2))
    }

    (notPreserveVarLocal ++ notPreserveDefLocal, notPreserveVarExternal ++ notPreserveDefExternal)
  }


  private def compRenamings(gs: NameGraph, t: Nominal, nodesToRename: Nodes): Renaming = {
    var renaming: Renaming = Map()
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

  private def compDependencyRenamings(nodesToRename: Set[Dependency], allNames: Set[String]): DependencyRenaming = {
    var renaming: DependencyRenaming = Map()

    for (v <- nodesToRename) {
      val fresh = gensym(v._2.name, allNames ++ renaming.values)
      renaming += (v -> fresh)
    }

    renaming
  }

  private def propagateRenamings[T <: NominalModular](gs: NameGraphModular, t: T, renamedDependencies: DependencyRenaming): (T, Renaming) = {
    val gt = t.resolveNames()
    var renaming: Renaming = Map()
    for ((dependency, dependencyRenaming) <- renamedDependencies) {
      for ((v, d) <- gs.EOut if d == dependency) {
        renaming ++= findConnectedNodes(gs, v).map(n => (n, dependencyRenaming)).toMap
      }
      for (v <- gt.V -- gs.V if v.name == dependency._2.name) {
        renaming ++= findConnectedNodes(gt, v).filter(n => !gs.V.contains(n)).map(n => (n, dependencyRenaming)).toMap
      }
    }
    val tRenamed = t.rename(renaming).asInstanceOf[T]
    (tRenamed, renaming)
  }

  private def nameFixCaptures[T <: NominalModular](gs: NameGraphModular, t: T, renamedDependencies: DependencyRenaming,
                                                   fixedModules: Set[Meta]): (T, Renaming, DependencyRenaming) = {
    val gt = t.resolveNames(renamedDependencies)
    val capture = findCapture(gs, gt, fixedModules)
    if (capture._1.isEmpty && capture._2.isEmpty)
      (t, Map(), Map())
    else {
      val renaming = compRenamings(gs, t, capture._1.values.toSet)
      val allNames = t.allNames.map(_.name) ++ fixedModules.flatMap(meta => meta._2 ++ meta._3).map(_.name)
      val virtuallyRenamedDependencies = compDependencyRenamings(capture._2.values.toSet, allNames)

      val tNew = t.rename(renaming).asInstanceOf[T]

      val (tNameFixed, recursiveRenaming, recursiveDependencyRenaming) = nameFixCaptures(gs, tNew, renamedDependencies ++ virtuallyRenamedDependencies, fixedModules)

      (tNameFixed, renaming ++ recursiveRenaming, virtuallyRenamedDependencies ++ recursiveDependencyRenaming)
    }
  }

  protected def nameFixModule[T <: NominalModular](gs: NameGraphModular, t: T, fixedModules: Set[Meta]): (T, Meta) = {
    val renamedDependencies = fixedModules.flatMap(m => m._4.map(r => ((m._1, r._1), r._2))).toMap

    // Step 1: Propagate renamings of dependencies
    val (tRenamed, propagationRenaming) = propagateRenamings(gs, t, renamedDependencies)

    // Step 2: Classic NameFix for captures
    // Note that the dependencyRenaming parameter is only used for recursion!
    // The actual dependencyRenaming calculated above is already applied in step 1 and no longer relevant.
    val (tFixedCaptures, renaming, virtualRenaming) = nameFixCaptures(gs, tRenamed, Map(), fixedModules)

    // Step 2: Fix name graph errors
    //val (tFixed2, renamingError) = nameFixErrors(gs, tFixed1)

    // Step 2: Find alternative renamings for exported names based on the fixed graph
    val (tFixedFinal, exportedNameRenamed) = nameFixFindAlternatives(tRenamed, tFixedCaptures, renaming, virtualRenaming)

    // Currently only a warning message, later additional handling
    if (exportedNameRenamed)
      println("NameFix failed to find a fix without renaming exported names!")

    val id = gs match {
      case NameGraphModular(ngID, _, _, _, _) => ngID
      case _ => null
    }
    val exportedNames = t match {
      case nm: NominalModular => nm.exportedNames
      case _ => Set[Name]()
    }

    (tFixedFinal, (id, exportedNames.filter(n => gs.V.contains(n.id)), exportedNames, renaming))
  }

  def nameFix[T <: NominalModular](modules: Set[(NameGraphModular, T)], fixedModules: Set[(T, Meta)]): Set[(T, Meta)] = {
    val fixableModules = modules.filter(_._1.EOut.forall(referredGraph => !modules.exists(_._1.ID == referredGraph._2._1)))
    if (fixableModules.isEmpty)
      sys.error("Encountered cyclic dependency of input modules! Cycles aren't supported by NameFix!")
    else {
      val currentModule = fixableModules.head
      val currentModuleFixed = nameFixModule(currentModule._1, currentModule._2, fixedModules.map(_._2))
      if ((modules - currentModule).isEmpty)
        fixedModules + currentModuleFixed
      else {
        nameFix(modules - currentModule, fixedModules + currentModuleFixed)
      }
    }
  }

  private def nameFixFindAlternatives[T <: NominalModular](t: T, tFixed: T, renaming: Renaming, virtualRenaming : DependencyRenaming): (T, Boolean) = {
    val gT = t.resolveNames()
    val gTFixed = tFixed.resolveNames()
    val exportedNames = tFixed.exportedNames

    var exportedNameRenamed = false
    var alternativeRenaming: Renaming = Map()

    for ((v, (nameGraphOld, dOld)) <- gT.EOut) {
      val (nameGraphNew, dNew) = gTFixed.EOut.get(v) match {
        case Some((nameGraph, d)) => (Some(nameGraph), Some(d))
        case None => gTFixed.E.get(v) match {
          case Some (d) => (None, Some(d))
          case None => (None, None)
        }
      }
    }

    // For each edge in the unfixed graph ...
    for ((v, dOld) <- gT.E) {
      // ... get the edge with the same source node in the fixed graph (there might also be no edge any more!)
      val (nameGraphNew, dNew) = gTFixed.E.get(v) match {
        case Some(d) => (None, Some(d))
        case None => gTFixed.EOut.get(v) match {
          case Some((nameGraph, d)) => (Some(nameGraph), Some(d))
          case None => (None, None)
        }
      }

      // Only need to handle edges that changed through fixing
      if (nameGraphNew.isDefined || dNew.isEmpty || dOld != dNew.get) {
          // Get all nodes connected to the old target node in the fixed graph
          val dOldReferences = findConnectedNodes(gTFixed, dOld)
          // Check if any of them is exported
          if (gTFixed.V.exists(v2 => dOldReferences.contains(v2) && exportedNames.exists(_.id == v2))) {

            // Get all nodes connected to the source node in the fixed graph
            // Note that this leads to the same result as using dNew if there is a new edge, but if there isn't, v itself is also a valid renaming option.
            val dNewReferences = findConnectedNodes(gTFixed, v)
            // Check if none of them is exported
            if (!gTFixed.V.exists(v2 => dNewReferences.contains(v2) && exportedNames.exists(_.id == v2))) {
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
      val (tFinal, exportedNameRenamedRecursive) = nameFixFindAlternatives(tFixedAlternative, tFixed, renaming ++ alternativeRenaming, virtualRenaming)

      (tFinal, exportedNameRenamed || exportedNameRenamedRecursive)
    }
  }
}