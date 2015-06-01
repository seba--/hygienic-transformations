package lang.lambda

import name.namegraph.NameGraphExtended
import name.{Identifier, Renaming}

/**
 * Created by seba on 01/08/14.
 */
case class Lam(x: Identifier, body: Exp) extends Exp {
  override def equals(a: Any) = a match {
    case Lam(x2, body2) => x.name == x2.name && body == body2
    case _ => false
  }
  override def hashCode = 17 * x.name.hashCode + 31 * body.hashCode()

  def allNames = body.allNames + x.name
  def rename(renaming: Renaming) = Lam(renaming(x), body.rename(renaming))
  def resolveNames(scope: Scope) = {
    val gbody = body.resolveNames(scope + (x.name -> x))
    NameGraphExtended(gbody.V + x, gbody.E)
  }

  def unsafeSubst(w: String, e: Exp) = if (x.name == w) this else Lam(x, body.unsafeSubst(w, e))

  def unsafeNormalize = Lam(x, body.unsafeNormalize)

  def alphaEqual(e: Exp, g: NameGraphExtended) = e match {
    case Lam(x2, body2) =>
      val E2 = g.E.flatMap(p => if (p._2 == x2) Some(p._1 -> Set(x)) else None)
      body.alphaEqual(body2, g + NameGraphExtended(Set(), E2))
    case _ => false
  }
}