package lang.lambda

import name.NameFix._
import name.NameGraph.{ID, NameGraph}
import name.Nominal

/**
 * Created by seba on 01/08/14.
 */
abstract class Exp extends Nominal {
  type Scope = Map[String,ID]

  def resolveNames: NameGraph = resolveNames(Map())
  def resolveNames(scope: Scope): NameGraph
  def rename(renaming: ID => ID): Exp

  def subst(x: String, e: Exp): Exp
  def safeSubst(w: String, e: Exp): Exp = nameFix(resolveNames, subst(w, e))
}