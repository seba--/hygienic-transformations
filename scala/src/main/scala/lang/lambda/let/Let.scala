package lang.lambda.let

import lang.lambda.Exp
import name.namegraph.NameGraphExtended
import name.{Identifier, Renaming}

/**
* Created by seba on 01/08/14.
*/
case class Let(x: Identifier, bound: Exp, body: Exp) extends Exp {
  override def equals(a: Any) = a match {
    case Let(x2, bound2, body2) => x.name == x2.name && bound == bound2 && body == body2
    case _ => false
  }

  override def hashCode = 17 * x.name.hashCode + 31 * bound.hashCode() + 47 * body.hashCode()

  def allNames = bound.allNames ++ body.allNames + x.name
  def rename(renaming: Renaming) = Let(renaming(x), bound.rename(renaming), body.rename(renaming))
  def resolveNames(scope: Scope) = {
    val gbound = bound.resolveNames(scope)
    val gbody = body.resolveNames(scope + (x.name -> x))
    gbound + gbody
  }

  def unsafeSubst(w: String, e: Exp) = {
    val bound2 = bound.unsafeSubst(w, e)
    if (x.name == w) Let(x, bound2, body) else Let(x, bound2, body.unsafeSubst(w, e))
  }

  def unsafeNormalize = body.unsafeSubst(x.name, bound).unsafeNormalize

  def alphaEqual(e: Exp, g: NameGraphExtended) = e match {
    case Let(x2, bound2, body2) =>
      if (!bound.alphaEqual(bound2, g))
        false
      else {
        val E2 = g.E.flatMap(p => if (p._2 == x2) Some(p._1 -> Set(x)) else None)
        body.alphaEqual(body2, g + NameGraphExtended(Set(), E2))
      }
    case _ => false
  }
}