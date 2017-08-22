package lang.lambdaref

import name._
import name.namegraph._
import ref.{Declaration, RefGraph, Reference}

case class App(e1: Exp, e2: Exp) extends Exp {

  override def retarget(retargeting: Map[Reference, Declaration]): Exp =
    App(e1.retarget(retargeting), e2.retarget(retargeting))

  override def resolveRefs: RefGraph = e1.resolveRefs + e2.resolveRefs

  def unsafeSubst(w: String, e: Exp) = App(e1.unsafeSubst(w, e), e2.unsafeSubst(w, e))

  def unsafeNormalize = e1.unsafeNormalize match {
    case Lam(x, body) => body.unsafeSubst(x, e2).unsafeNormalize
    case v1 => App(v1, e2)
  }
}