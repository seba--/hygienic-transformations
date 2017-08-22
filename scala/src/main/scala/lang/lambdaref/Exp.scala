package lang.lambdaref

import name._
import ref.RefFix._
import ref.{Declaration, RefGraph, Reference, Structural}

abstract class Exp extends Structural {
  type Scope = Map[String, Set[Identifier]]

  def resolveRefs: RefGraph
  def retarget(retargeting: Map[Reference, Declaration]): Exp

  def substGraph(x: String, e: Exp): Exp
  def unsafeSubst(w: String, e: Exp): Exp = asNominal.unsafeSubst(w, e.asNominal).asStructural
  def subst(w: String, e: Exp): Exp = refFix(resolveRefs, unsafeSubst(w, e))

  def normalizeGraph: Exp
  def unsafeNormalize: Exp = asNominal.unsafeNormalize.asStructural
  def normalize = refFix(resolveRefs, unsafeNormalize)

  override def asNominal(implicit gensym: Gensym = new Gensym): lang.lambda.Exp
}