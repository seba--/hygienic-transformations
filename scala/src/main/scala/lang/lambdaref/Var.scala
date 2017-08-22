package lang.lambdaref

import ref.{Declaration, RefGraph, Reference, Structural}

object Var {
  def apply(target: Lam): Var = Var(Some(target))
}
case class Var(var _target: Option[Lam] = None) extends Exp with Reference {
  def target = _target.getOrElse(throw new UnsupportedOperationException(s"Cannot apply operation before setting placeholder target"))
  def initialize(target: Lam) =
    if (_target.isEmpty) _target = Some(target)
    else throw new UnsupportedOperationException(s"Cannot reset target of placeholder var again, was $this")

  override def retarget(newtarget: Declaration): Var = if (newtarget == target) this else Var(newtarget.asInstanceOf[Lam])
  override def retarget(retargeting: Map[Reference, Declaration]): Exp = retargeting.get(this) match {
    case None => this
    case Some(newtarget) => retarget(newtarget)
  }

  override def resolveRefs: RefGraph = RefGraph(Set(), Set(this))

  def substGraph(w: String, e: Exp) = if (target.x == w) e else this

  def normalizeGraph = this

  override def toString = s"Var(${target.x})"

  override def equiv(obj: Structural, eqDecls: Map[Declaration, Declaration]): Boolean = obj match {
    case that: Var => id == that.id || eqDecls.get(this.target).get == that.target
    case _ => false
  }
}
