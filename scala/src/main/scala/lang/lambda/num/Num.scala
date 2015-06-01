package lang.lambda.num

import lang.lambda.Exp
import name.namegraph.NameGraphExtended
import name.Renaming

/**
 * Created by seba on 01/08/14.
 */
case class Num(v: Int) extends Exp {
  def allNames = Set()
  def rename(renaming: Renaming) = this
  def resolveNames(scope: Scope) = NameGraphExtended(Set(), Map())

  def unsafeSubst(w: String, e: Exp) = this

  def unsafeNormalize = this

  def alphaEqual(e: Exp, g: NameGraphExtended) = e match {
    case Num(v2) => v == v2
    case _ => false
  }
}