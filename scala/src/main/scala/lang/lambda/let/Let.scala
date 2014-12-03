package lang.lambda.let

import lang.lambda.{QualifiedVar, Exp}
import name.{Edges, Name, NameGraph}

/**
* Created by seba on 01/08/14.
*/
case class Let(x: Name, bound: Exp, body: Exp) extends Exp {
  def allNames = bound.allNames ++ body.allNames + x.id
  def rename(renaming: Renaming) = Let(renaming(x), bound.rename(renaming), body.rename(renaming))
  def resolveNames(scope: Scope, modularScope: ModularScope) = {
    val gbound = bound.resolveNames(scope, modularScope)
    val gbody = body.resolveNames(scope + (x.name -> x.id), modularScope)
    gbound ++ gbody ++ NameGraph(Set(x.id), Map() : Edges)
  }

  def unsafeSubst(w: String, e: Exp) = {
    val bound2 = bound.unsafeSubst(w, e)
    if (x.name == w) Let(x, bound2, body) else Let(x, bound2, body.unsafeSubst(w, e))
  }

  def unsafeNormalize = body.unsafeSubst(x.name, bound).unsafeNormalize

  def alphaEqual(e: Exp, g: NameGraph) = e match {
    case Let(x2, bound2, body2) =>
      if (!bound.alphaEqual(bound2, g))
        false
      else {
        val E2 = g.E.flatMap(p => if (p._2 == x2.id) Some(p._1 -> x.id) else None)
        body.alphaEqual(body2, g + E2)
      }
    case _ => false
  }

  override def replaceByQualifiedVar(name: Name, qualifiedVar: QualifiedVar) =
    Let(x, bound.replaceByQualifiedVar(name, qualifiedVar), body.replaceByQualifiedVar(name, qualifiedVar))
}