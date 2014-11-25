package lang.lambda

import name.{Edges, Name, NameGraph}

/**
 * Created by seba on 01/08/14.
 */
case class Lam(x: Name, body: Exp) extends Exp {
  def allNames = body.allNames + x.id
  def rename(renaming: Renaming) = Lam(renaming(x), body.rename(renaming))
  def resolveNames(scope: Scope) = {
    val gbody = body.resolveNames(scope + (x.name -> x.id))
    gbody ++ NameGraph(Set(x.id), Map() : Edges)
  }

  def unsafeSubst(w: String, e: Exp) = if (x.name == w) this else Lam(x, body.unsafeSubst(w, e))

  def unsafeNormalize = Lam(x, body.unsafeNormalize)

  def alphaEqual(e: Exp, g: NameGraph) = e match {
    case Lam(x2, body2) =>
      val E2 = g.E.flatMap(p => if (p._2 == x2.id) Some(p._1 -> x.id) else None)
      body.alphaEqual(body2, g + E2)
    case _ => false
  }
}