package lang.lambda

import name._
import name.namefix.NameFix._
import name.namegraph.NameGraphExtended
import ref.{Declaration, Structural}

/**
 * Created by seba on 01/08/14.
 */
abstract class Exp extends Nominal {
  type Scope = Map[String,Set[Identifier]]

  def resolveNames: NameGraphExtended = resolveNames(Map())
  def resolveNames(scope: Scope): NameGraphExtended
  def rename(renaming: Renaming): Exp

  def unsafeSubst(x: String, e: Exp): Exp
  def subst(w: String, e: Exp): Exp = nameFixExtended(resolveNames, unsafeSubst(w, e))

  def unsafeNormalize: Exp
  def normalize = nameFixExtended(resolveNames, unsafeNormalize)

  def alphaEqual(e: Exp): Boolean = {
    val gthis = resolveNames
    val ge = e.resolveNames
    alphaEqual(e, gthis + ge)
  }
  def alphaEqual(e: Exp, g: NameGraphExtended): Boolean

  override def asStructural = asStructural(Map())
  def asStructural(g: Map[String, Declaration]): lang.lambdaref.Exp
}