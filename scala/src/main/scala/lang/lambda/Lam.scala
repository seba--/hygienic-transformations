package lang.lambda

import name.NameGraph.{NameGraph, ID}

/**
 * Created by seba on 01/08/14.
 */
case class Lam(x: ID, body: Exp) extends Exp {
  def allIDs = body.allIDs + x
  def rename(renaming: ID => ID) = Lam(renaming(x), body.rename(renaming))
  def resolveNames(scope: Scope) = {
    val gbody = body.resolveNames(scope + (x.name -> x))
    NameGraph(gbody.V + x, gbody.E)
  }

  override def hashCode = x.name.hashCode + body.hashCode
  override def equals(a: Any) =
    a.isInstanceOf[Lam] &&
      x.name == a.asInstanceOf[Lam].x.name &&
      body == a.asInstanceOf[Lam].body

  def subst(w: String, e: Exp) = if (x.name == w) this else Lam(x, body.subst(w, e))
}