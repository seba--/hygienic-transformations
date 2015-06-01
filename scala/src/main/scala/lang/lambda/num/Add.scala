package lang.lambda.num

import lang.lambda.Exp
import name.namegraph.NameGraph
import name.Renaming

/**
 * Created by seba on 01/08/14.
 */
case class Add(e1: Exp, e2: Exp) extends Exp {
  def allNames = e1.allNames ++ e2.allNames
  def rename(renaming: Renaming) = Add(e1.rename(renaming), e2.rename(renaming))
  def resolveNames(scope: Scope) = {
    val g1 = e1.resolveNames(scope)
    val g2 = e2.resolveNames(scope)
    NameGraph(g1.V ++ g2.V, g1.E ++ g2.E)
  }

  def unsafeSubst(w: String, e: Exp) = Add(e1.unsafeSubst(w, e), e2.unsafeSubst(w, e))

  def unsafeNormalize = (e1.unsafeNormalize, e2.unsafeNormalize) match {
    case (Num(v1), Num(v2)) => Num(v1 + v2)
    case (v1, v2) => Add(v1, v2)
  }

  def alphaEqual(e: Exp, g: NameGraph) = e match {
    case Add(e3, e4) => e1.alphaEqual(e3, g) && e2.alphaEqual(e4, g)
    case _ => false
  }
}