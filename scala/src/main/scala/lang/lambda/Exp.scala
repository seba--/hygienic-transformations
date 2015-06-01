package lang.lambda

import name.namefix.NameFix
import NameFix._
import name._
import name.namegraph.NameGraph

/**
 * Created by seba on 01/08/14.
 */
abstract class Exp extends Nominal {
  type Scope = Map[String,Identifier]

  def resolveNames = resolveNames(Map())
  def resolveNames(scope: Scope): NameGraph
  def rename(renaming: Renaming): Exp

  def unsafeSubst(x: String, e: Exp): Exp
  def subst(w: String, e: Exp): Exp = nameFix(resolveNames, unsafeSubst(w, e))

  def unsafeNormalize: Exp
  def normalize = nameFix(resolveNames, unsafeNormalize)

  def alphaEqual(e: Exp): Boolean = {
    val gthis = resolveNames
    val ge = e.resolveNames
    alphaEqual(e, gthis + ge)
  }
  def alphaEqual(e: Exp, g: NameGraph): Boolean
}