package lang.lambdaref

import name.namegraph.NameGraphExtended
import name.Name
import ref.{Declaration, RefGraph, Reference, Structural}

case class Lam(x: Name, body: Exp) extends Exp with Declaration {

  override def retarget(retargeting: Map[Reference, Declaration]): Exp =
    Lam(x, body.retarget(retargeting)).withID(this)

  override def resolveRefs: RefGraph = {
    val gbody = body.resolveRefs
    RefGraph(gbody.decls + this, gbody.refs)
  }

  def substGraph(w: String, e: Exp) = if (x == w) this else Lam(x, body.substGraph(w, e)).withID(this)

  def normalizeGraph = Lam(x, body.normalizeGraph).withID(this)

  override def equiv(obj: Structural, eqDecls: Map[Declaration, Declaration]): Boolean = obj match {
    case that: Lam => this.x == that.x && this.body.equiv(that.body, eqDecls + (this -> that))
    case _ => false
  }
}