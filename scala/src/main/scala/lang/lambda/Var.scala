package lang.lambda

import name.Name
import name.NameGraph.NameGraph

/**
 * Created by seba on 01/08/14.
 */
case class Var(x: Name) extends Exp {
  def allNames = Set(x)
  def rename(renaming: Renaming) = Var(renaming(x))
  def resolveNames(scope: Scope) =
    if (scope.contains(x.name))
      NameGraph(Set(x.id), Map(x.id -> scope(x.name)))
    else
      NameGraph(Set(x.id), Map())

  override def hashCode = x.name.hashCode
  override def equals(a: Any) = a.isInstanceOf[Var] && x.name == a.asInstanceOf[Var].x.name

  def subst(w: String, e: Exp) = if (x.name == w) e else this

  def normalize = this

  def alphaEqual(e: Exp, g: NameGraph) = e match {
    case Var(xe) => (g.E.get(x.id), g.E.get(xe.id)) match {
      case (None, None) => true // both free
      case (Some(d1), Some(d2)) => d1 == d2 // bound to the same decl
      case _ => false
    }
    case _ => false
  }

}