package lang.lambda

import name.NameGraph.{NameGraph, ID}

/**
 * Created by seba on 01/08/14.
 */
case class App(e1: Exp, e2: Exp) extends Exp {
  def allIDs = e1.allIDs ++ e2.allIDs
  def rename(renaming: ID => ID) = App(e1.rename(renaming), e2.rename(renaming))
  def resolveNames(scope: Scope) = {
    val g1 = e1.resolveNames(scope)
    val g2 = e2.resolveNames(scope)
    NameGraph(g1.V ++ g2.V, g1.E ++ g2.E)
  }

  def subst(w: String, e: Exp) = App(e1.subst(w, e), e2.subst(w, e))
}