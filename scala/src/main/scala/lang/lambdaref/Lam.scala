package lang.lambdaref

import name.Identifier.ID
import name.{Gensym, Identifier, Name}
import ref.{Declaration, RefGraph, Reference, Structural}

object Lam {
  def apply(x: Name, body: Lam => Exp): Lam = {
    val lam = Lam(x, null.asInstanceOf[Exp])
    val b = body(lam)
    lam._body = b
    lam
  }
}

case class Lam(x: Name, private var _body: Exp) extends Exp with Declaration {
  def body = _body

  override def retarget(retargeting: Map[Reference, Option[Declaration]]): Exp =
    Lam(x, body.retarget(retargeting)).withID(this)

  override def resolveRefs: RefGraph = {
    val gbody = body.resolveRefs
    RefGraph(gbody.decls + this, gbody.refs)
  }

  def substGraph(w: String, e: Exp) = if (x == w) this else Lam(x, body.substGraph(w, e)).withID(this)

  def normalizeGraph = Lam(x, body.normalizeGraph).withID(this)

  override def equiv(obj: Structural, eqDecls: Map[ID, Declaration]): Boolean = obj match {
    case that: Lam => this.x == that.x && this.body.equiv(that.body, eqDecls + (this.id -> that))
    case _ => false
  }

  override def asNominal(implicit gensym: Gensym) = lang.lambda.Lam(new Identifier(x, id), body.asNominal)
}