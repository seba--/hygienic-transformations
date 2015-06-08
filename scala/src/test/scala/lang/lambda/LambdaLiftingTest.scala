package lang.lambda

import lang.lambda.let.Let
import lang.lambda.module.Module
import lang.lambda.num.{Num, Add}
import lang.lambda.trans.LambdaLiftingTransformation
import name.namefix.NameFix
import org.scalatest.FunSuite

/**
 * Created by nico on 08.06.15.
 */
class LambdaLiftingTest extends FunSuite {
  val module = Module("module", Set(), Map(
    ("test", (Let("y", Num(1), App(Lam("x", Add(Var("y"), Var("x"))), Var("y"))), true))))

  test ("lambda lifting") {
    val liftedModule = LambdaLiftingTransformation.transform(module)
    val replaced = liftedModule.defs.find(_._1.name == "test").get._2._1 match {
      case Let(_, _, App(x, _)) => x
    }

    val liftedName = replaced match {
      case App(Var(x), Var(y)) =>
        assert(y.name == "y")
        x
      case _ => fail()
    }

    assert(liftedModule.defs.contains(liftedName))
    assert(liftedModule.defs(liftedName) match {
      case (Lam(y, Lam(x, Add(Var(y1), Var(x1)))), false) => y.name == y1.name && x.name == x1.name
      case _ => fail()
    })
  }

  val module2 = Module("module", Set(), Map(
    ("test", (Let("y", Num(1), App(Lam("x", Add(Var("y"), Var("x"))), Var("y"))), true)),
    ("f_0", (Add(Num(1), Num(2)), true))))
  val f_0Orig = module2.defs.find(_._1.name == "f_0").get

  test ("name-fix for lambda lifting") {
    val liftedModule = LambdaLiftingTransformation.transform(module2)
    val liftedGraph = liftedModule.resolveNamesModular

    // Capture
    assert(liftedGraph.E.exists(_._2.contains(f_0Orig._1)))

    val fixedModule = NameFix.nameFixModular(module2.resolveNamesModular, liftedModule, Set())
    val fixedGraph = fixedModule.resolveNamesModular

    // Capture solved
    assert(!fixedGraph.E.exists(_._2.contains(f_0Orig._1)))

    // NameFix avoided renaming identifiers that are in the module interface
    assert(fixedGraph.I == liftedGraph.I)
    info(fixedModule.toString)
  }
}
