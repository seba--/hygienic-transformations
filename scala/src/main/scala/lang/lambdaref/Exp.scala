package lang.lambdaref

import name._
import ref.RefFix._
import ref.{Declaration, RefGraph, Reference, Structural}

abstract class Exp extends Structural {
  type Scope = Map[String, Set[Identifier]]

  def resolveRefs: RefGraph
  def retarget(retargeting: Map[Reference, Declaration]): Exp

  def unsafeSubst(x: String, e: Exp): Exp
  def subst(w: String, e: Exp): Exp = refFix(resolveRefs, unsafeSubst(w, e))

  def unsafeNormalize: Exp
  def normalize = refFix(resolveRefs, unsafeNormalize)
}