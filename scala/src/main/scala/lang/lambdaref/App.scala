package lang.lambdaref

import name.Gensym
import name.Identifier.ID
import ref.{Declaration, RefGraph, Reference, Structural}

case class App(e1: Exp, e2: Exp) extends Exp {

  override def retarget(retargeting: Map[Reference, Option[Declaration]]): Exp =
    App(e1.retarget(retargeting), e2.retarget(retargeting))

  override def resolveRefs: RefGraph = e1.resolveRefs + e2.resolveRefs

  def substGraph(w: String, e: Exp) = App(e1.substGraph(w, e), e2.substGraph(w, e))

  def normalizeGraph = e1.normalizeGraph match {
    case Lam(x, body) => body.substGraph(x, e2).normalizeGraph
    case v1 => App(v1, e2.normalizeGraph)
  }

  override def equiv(obj: Structural, eqDecls: Map[ID, Declaration]): Boolean = obj match {
    case that: App => this.e1.equiv(that.e1, eqDecls) && this.e2.equiv(that.e2, eqDecls)
    case _ => false
  }

  override def asNominal(implicit gensym: Gensym) = lang.lambda.App(e1.asNominal, e2.asNominal)
}