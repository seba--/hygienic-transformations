package lang.lambda

import lang.lambdaref
import name.namegraph.NameGraphExtended
import name.{Identifier, Renaming}
import ref.Declaration

/**
 * Created by seba on 01/08/14.
 */
case class Var(x: Identifier) extends Exp {
  override def equals(a: Any) = a match {
    case Var(x2) => x.name == x2.name
    case _ => false
  }
  override def hashCode = x.name.hashCode()

  def allNames = Set(x.name)
  def rename(renaming: Renaming) = Var(renaming(x))
  def resolveNames(scope: Scope) =
    if (scope.contains(x.name))
      NameGraphExtended(Set(x), Map(x -> scope(x.name)))
    else
      NameGraphExtended(Set(x), Map())

  def unsafeSubst(w: String, e: Exp) = if (x.name == w) e else this

  def unsafeNormalize = this

  def alphaEqual(e: Exp, g: NameGraphExtended) = e match {
    case Var(xe) => (g.E.get(x), g.E.get(xe)) match {
      case (None, None) => true // both free
      case (Some(d1), Some(d2)) => d1 == d2 // bound to the same decl
      case _ => false
    }
    case _ => false
  }

  override def asStructural(g: Map[String, Declaration]): lambdaref.Exp =
    g.get(x.name) match {
      case None => lang.lambdaref.Var(x.name).withID(x.id)
      case Some(d) => lang.lambdaref.Var(d.asInstanceOf[lang.lambdaref.Lam]).withID(x.id)
    }
}