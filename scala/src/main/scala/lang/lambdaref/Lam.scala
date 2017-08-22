package lang.lambdaref

import name.namegraph.NameGraphExtended
import name.Name
import ref.{Declaration, RefGraph, Reference}

case class Lam(x: Name, body: Exp) extends Exp with Declaration {

  override def retarget(retargeting: Map[Reference, Declaration]): Exp =
    Lam(x, body.retarget(retargeting))

  override def resolveRefs: RefGraph = {
    val gbody = body.resolveRefs
    RefGraph(gbody.decls + this, gbody.refs)
  }

  def unsafeSubst(w: String, e: Exp) = if (x == w) e else Lam(x, body.unsafeSubst(w, e))

  def unsafeNormalize = Lam(x, body.unsafeNormalize)
}