package lang.lambda.num

import lang.lambda.{QualifiedVar, Exp}
import name.{Name, NameGraph}

/**
 * Created by seba on 01/08/14.
 */
case class Num(v: Int) extends Exp {
  def allNames = Set()
  def rename(renaming: RenamingFunction) = this
  def resolveNames(scope: Scope, modularScope: ModularScope) = NameGraph(Map())

  def unsafeSubst(w: String, e: Exp) = this

  def unsafeNormalize = this

  def alphaEqual(e: Exp, g: NameGraph) = e match {
    case Num(v2) => v == v2
    case _ => false
  }

  override def replaceByQualifiedVar(name: Name, qualifiedVar: QualifiedVar) = this
}