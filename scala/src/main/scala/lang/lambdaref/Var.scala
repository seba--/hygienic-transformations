package lang.lambdaref

import name.Identifier.ID
import name.{Gensym, GensymPure, Identifier, Name}
import ref.{Declaration, RefGraph, Reference, Structural}

object Var {
  def apply(target: Declaration): Var = Var(Some(target), None)
  def apply(placeholderName: String): Var = Var(None, Some(placeholderName))
}
case class Var(var _target: Option[Declaration], val placeholderName: Option[Name]) extends Exp with Reference {
  def target = _target.getOrElse(throw new UnsupportedOperationException(s"Cannot apply operation before setting placeholder target"))
  def name = target.asInstanceOf[Lam].x

  override def retarget(newtarget: Declaration): Var = if (newtarget == target) this else Var(newtarget)
  override def retarget(retargeting: Map[Reference, Declaration]): Exp = retargeting.get(this) match {
    case None => this
    case Some(newtarget) => retarget(newtarget)
  }

  override def resolveRefs: RefGraph = RefGraph(Set(), Set(this))

  def substGraph(w: String, e: Exp) = if (name == w) e else this

  def normalizeGraph = this

  override def toString = _target match {
    case Some(_) => s"Var($name)"
    case None => s"Var($placeholderName)"
  }

  override def equiv(obj: Structural, eqDecls: Map[ID, Declaration]): Boolean = obj match {
    case that: Var => (this._target, that._target) match {
      case (Some(t1), Some(t2)) => eqDecls(t1.id).id == t2.id
      case (None, None) => this.id == that.id
      case _ => false
    }
    case _ => false
  }

  override def asNominal(implicit gensym: Gensym) = _target match {
    case Some(_) => lang.lambda.Var(new Identifier(name, id))
    case None => lang.lambda.Var(new Identifier(placeholderName.get, id))
  }
}
