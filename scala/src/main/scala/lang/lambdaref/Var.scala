package lang.lambdaref

import name.namegraph.NameGraphExtended
import ref.{Declaration, RefGraph, Reference}

case class Var(target: Lam) extends Exp with Reference {

  override def retarget(newtarget: Declaration): Var = Var(newtarget.asInstanceOf[Lam])
  override def retarget(retargeting: Map[Reference, Declaration]): Exp = retargeting.get(this) match {
    case None => this
    case Some(newtarget) => retarget(newtarget)
  }

  override def resolveRefs: RefGraph = RefGraph(Set(), Set(this))

  def unsafeSubst(w: String, e: Exp) = if (target.x == w) e else this

  def unsafeNormalize = this
}