package lang.lambdaref

import name._
import ref.RefFix._
import ref.{Declaration, RefGraph, Reference, Structural}

abstract class Exp extends Structural {
  type Scope = Map[String, Set[Identifier]]

  def resolveRefs: RefGraph
  def retarget(retargeting: Map[Reference, Declaration]): Exp

  def substGraph(x: String, e: Exp): Exp
  def subst(w: String, e: Exp): Exp = refFix(resolveRefs, substGraph(w, e))

  def normalizeGraph: Exp
  def normalize = refFix(resolveRefs, normalizeGraph)
}