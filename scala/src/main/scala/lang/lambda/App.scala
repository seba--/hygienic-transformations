package lang.lambda

import name.{Name, NameGraph}

/**
 * Created by seba on 01/08/14.
 */
case class App(e1: Exp, e2: Exp) extends Exp {
  def allNames = e1.allNames ++ e2.allNames
  def rename(renaming: RenamingFunction) = App(e1.rename(renaming), e2.rename(renaming))
  def resolveNames(scope: Scope, modularScope: ModularScope) = {
    val g1 = e1.resolveNames(scope, modularScope)
    val g2 = e2.resolveNames(scope, modularScope)
    g1 ++ g2
  }

  def unsafeSubst(w: String, e: Exp) = App(e1.unsafeSubst(w, e), e2.unsafeSubst(w, e))

  def unsafeNormalize = e1.unsafeNormalize match {
    case Lam(x, body) => body.unsafeSubst(x.name, e2).unsafeNormalize
    case v1 => App(v1, e2)
  }

  def alphaEqual(e: Exp, g: NameGraph) = e match {
    case App(e3, e4) => e1.alphaEqual(e3, g) && e2.alphaEqual(e4, g)
    case _ => false
  }

  override def replaceByQualifiedVar(name: Name, qualifiedVar: QualifiedVar) =
    App(e1.replaceByQualifiedVar(name, qualifiedVar), e2.replaceByQualifiedVar(name, qualifiedVar))
}