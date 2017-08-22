package lang.lambdaref

import name.Identifier.ID
import name.{Gensym, GensymPure, Identifier}
import ref.{Declaration, RefGraph, Reference, Structural}

object Var {
  def apply(target: Declaration): Var = Var(Some(target))
}
case class Var(var _target: Option[Declaration] = None) extends Exp with Reference {
  def target = _target.getOrElse(throw new UnsupportedOperationException(s"Cannot apply operation before setting placeholder target"))
  def name = target.asInstanceOf[Lam].x

  def initialize(target: Declaration) =
    if (_target.isEmpty) _target = Some(target)
    else throw new UnsupportedOperationException(s"Cannot reset target of placeholder var again, was $this")

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
    case None => "Var($placeholder)"
  }

  override def equiv(obj: Structural, eqDecls: Map[ID, Declaration]): Boolean = obj match {
    case that: Var => _id == that._id || eqDecls.get(this.target.id).get.id == that.target.id
    case _ => false
  }

  override def asNominal(implicit gensym: Gensym) = _target match {
    case Some(_) => lang.lambda.Var(new Identifier(name, id))
    case None => lang.lambda.Var(new Identifier(gensym.fresh("placeholder"), id))
  }
}
