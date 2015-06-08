package lang.lambda.trans

import lang.lambda.let.Let
import lang.lambda.num.{Num, Add}
import lang.lambda.{Var, App, Lam, Exp}
import lang.lambda.module.Module
import lang.lambda.module.Module.Def
import name.{Name, Gensym, Identifier}

object LambdaLiftingTransformation {
  def transform(module: Module): Module = {
    var newDefs = Map[Identifier, Def]()
    for ((defName, (defBody, defExport)) <- module.defs) {

      val (newDef, liftedLams) = skipNestedLambdas(defBody)
      newDefs += defName -> (newDef, defExport)
      newDefs ++= liftedLams.map(l => (l._1, (l._2, false)))
    }
    Module(module.name, module.imports, newDefs)
  }

  protected def skipNestedLambdas(expr: Exp, usedFunNames: Set[Name] = Set()): (Exp, Set[(Identifier, Exp)]) = {
    expr match {
      case Lam(x, body) => {
        val (liftedBody, liftedLam) = skipNestedLambdas(body, usedFunNames)
        (Lam(x, liftedBody), liftedLam)
      }
      case _ => lambdaLift(expr, usedFunNames)
    }
  }

  protected def lambdaLift(expr: Exp, usedFunNames: Set[Name] = Set()): (Exp, Set[(Identifier, Exp)]) = {
    expr match {
      case Lam(x, body) => {
        val (liftedBody, liftedLams) = skipNestedLambdas(body, usedFunNames)
        val exprGraph = expr.resolveNames
        val freeNames = exprGraph.V.filter(v => !exprGraph.E.contains(v) && !exprGraph.E.values.exists(_.contains(v))).toSeq
        val freeNamesUnique = freeNames.groupBy(_.name).map(_._2.head).toSeq
        val newName = Identifier(Gensym.gensym("f", expr.allNames ++ usedFunNames ++ liftedLams.map(_._1.name)))
        val newLam = freeNamesUnique.foldLeft(Lam(x, liftedBody))((curr, next) => Lam(next, curr))
        val newApp = freeNamesUnique.reverse.foldLeft(Var(newName):Exp)((curr, next) => App(curr, Var(next)))
        (newApp, liftedLams + ((newName, newLam)))
      }
      case App(e1, e2) => {
        val (liftedE1, liftedE1Lams) = lambdaLift(e1, usedFunNames)
        val (liftedE2, liftedE2Lams) = lambdaLift(e2, usedFunNames ++ liftedE1Lams.map(_._1.name))
        (App(liftedE1, liftedE2), liftedE1Lams ++ liftedE2Lams)
      }
      case Let(x, bound, body) => {
        val (liftedBound, liftedBoundLams) = lambdaLift(bound, usedFunNames)
        val (liftedBody, liftedBodyLams) = lambdaLift(body, usedFunNames ++ liftedBoundLams.map(_._1.name))
        (Let(x, liftedBound, liftedBody), liftedBoundLams ++ liftedBodyLams)
      }
      case Add(n, m) => {
        val (liftedN, liftedNLams) = lambdaLift(n, usedFunNames)
        val (liftedM, liftedMLams) = lambdaLift(m, usedFunNames ++ liftedNLams.map(_._1.name))
        (Add(liftedN, liftedM), liftedNLams ++ liftedMLams)
      }
      case Num(n) => (expr, Set())
      case Var(x) => (expr, Set())
    }
  }
}
