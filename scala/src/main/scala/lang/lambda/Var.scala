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
}