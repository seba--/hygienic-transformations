package lang.lambda

import name.Name
import name.NameGraph.NameGraph

/**
 * Created by seba on 01/08/14.
 */
case class Lam(x: Name, body: Exp) extends Exp {
  def allNames = body.allNames + x
  def rename(renaming: Renaming) = Lam(renaming(x), body.rename(renaming))
  def resolveNames(scope: Scope) = {
    val gbody = body.resolveNames(scope + (x.name -> x.id))
    NameGraph(gbody.V + x.id, gbody.E)
  }

  override def hashCode = x.name.hashCode + body.hashCode
  override def equals(a: Any) =
    a.isInstanceOf[Lam] &&
      x.name == a.asInstanceOf[Lam].x.name &&
      body == a.asInstanceOf[Lam].body

  def unsafeSubst(w: String, e: Exp) = if (x.name == w) this else Lam(x, body.unsafeSubst(w, e))

  def unsafeNormalize = Lam(x, body.unsafeNormalize)

  def alphaEqual(e: Exp, g: NameGraph) = e match {
    case Lam(x2, body2) =>
      val E2 = g.E.flatMap(p => if (p._2 == x2.id) Some(p._1 -> x.id) else None)
      body.alphaEqual(body2, NameGraph(g.V, g.E ++ E2))
    case _ => false
  }
}