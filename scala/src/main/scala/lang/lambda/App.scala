package lang.lambda

import name.NameGraph.NameGraph

/**
 * Created by seba on 01/08/14.
 */
case class App(e1: Exp, e2: Exp) extends Exp {
  def allNames = e1.allNames ++ e2.allNames
  def rename(renaming: Renaming) = App(e1.rename(renaming), e2.rename(renaming))
  def resolveNames(scope: Scope) = {
    val g1 = e1.resolveNames(scope)
    val g2 = e2.resolveNames(scope)
    NameGraph(g1.V ++ g2.V, g1.E ++ g2.E)
  }

  def subst(w: String, e: Exp) = App(e1.subst(w, e), e2.subst(w, e))

  def normalize = e1.normalize match {
    case Lam(x, body) => body.subst(x.name, e2).normalize
    case v1 => App(v1, e2)
  }

  def alphaEqual(e: Exp, g: NameGraph) = e match {
    case App(e3, e4) => e1.alphaEqual(e3, g) && e2.alphaEqual(e4, g)
    case _ => false
  }
}