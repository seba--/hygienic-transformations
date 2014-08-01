package lang.lambda

import name.NameGraph.{NameGraph, ID}

/**
 * Created by seba on 01/08/14.
 */
case class Var(x: ID) extends Exp {
  def allIDs = Set(x)
  def rename(renaming: ID => ID) = Var(renaming(x))
  def resolveNames(scope: Scope) =
    if (scope.contains(x.name))
      NameGraph(Set(x), Map(x -> scope(x.name)))
    else
      NameGraph(Set(x), Map())

  override def hashCode = x.name.hashCode
  override def equals(a: Any) = a.isInstanceOf[Var] && x.name == a.asInstanceOf[Var].x.name

  def subst(w: String, e: Exp) = if (x.name == w) e else this

  def normalize = this

  def alphaEqual(e: Exp, g: NameGraph) = e match {
    case Var(xe) => (g.E.get(x), g.E.get(xe)) match {
      case (None, None) => true // both free
      case (Some(d1), Some(d2)) => d1 == d2 // bound to the same decl
      case _ => false
    }
    case _ => false
  }

}