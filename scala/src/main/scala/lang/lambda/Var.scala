package lang.lambda

import name.{Edges, Name, NameGraph}

/**
 * Created by seba on 01/08/14.
 */
abstract class Access extends Exp {
  val x: Name
  override def allNames = Set(x.id)

  override def replaceByQualifiedVar(name: Name, qualifiedVar: QualifiedVar) = {
    if (x.id == name.id) qualifiedVar
    else this
  }
}

case class Var(x: Name) extends Access {
  def rename(renaming: Renaming) = Var(renaming(x))
  def resolveNames(scope: Scope, modularScope: ModularScope) =
    if (scope.contains(x.name))
      NameGraph(Set(x.id), Map(x.id -> scope(x.name)))
    else
      NameGraph(Set(x.id), Map() : Edges)

  override def hashCode = x.name.hashCode
  override def equals(a: Any) = a.isInstanceOf[Var] && x.name == a.asInstanceOf[Var].x.name

  def unsafeSubst(w: String, e: Exp) = if (x.name == w) e else this

  def unsafeNormalize = this

  def alphaEqual(e: Exp, g: NameGraph) = e match {
    case Var(xe) => (g.E.get(x.id), g.E.get(xe.id)) match {
      case (None, None) => true // both free
      case (Some(d1), Some(d2)) => d1 == d2 // bound to the same decl
      case _ => false
    }
    case _ => false
  }
}

case class QualifiedVar(qualifier: Name, x: Name) extends Access {
  def rename(renaming: Renaming) = QualifiedVar(qualifier, renaming(x))

  override def resolveNames(scope: Scope, modularScope: ModularScope) = {
    val ref = (qualifier.name, x.name)
    if (modularScope.contains(ref))
      NameGraph(Set(qualifier.id, x.id), Map(x.id -> modularScope(ref)._2))
    else
      NameGraph(Set(x.id), Map(): Edges)
  }

  override def hashCode = x.name.hashCode
  override def equals(a: Any) = a.isInstanceOf[Var] && x.name == a.asInstanceOf[Var].x.name

  def unsafeSubst(w: String, e: Exp) = if (x.name == w) e else this

  def unsafeNormalize = this

  def alphaEqual(e: Exp, g: NameGraph) = e match {
    case QualifiedVar(q, xe) => (g.E.get(x.id), g.E.get(xe.id)) match {
      case (None, None) => true // both free
      case (Some(d1), Some(d2)) => d1 == d2 // bound to the same decl
      case _ => false
    }
    case _ => false
  }
}