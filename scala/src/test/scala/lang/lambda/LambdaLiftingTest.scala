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

    val liftAgain = LambdaLiftingTransformation.transform(liftedModule)
    assert(liftAgain == liftedModule)
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

    // NameFix avoided renaming exported identifiers
    assert(fixedGraph.I == liftedGraph.I)
    info(fixedModule.toString)
  }

  val moduleBase = Module("base", Set(), Map(("f_0", (Num(1), true))))
  val module3 = Module("module", Set("base"), Map(
    ("test", (Let("y", Var("f_0"), App(Lam("x", Add(Var("y"), Var("x"))), Var("y"))), true))))
  val f_0base = moduleBase.defs.find(_._1.name == "f_0").get._1

  test ("name-fix for lambda lifting with imported captures") {
    module3.link(moduleBase.interface)
    val liftedModule = LambdaLiftingTransformation.transform(module3, exportLiftedLams = true)
    val liftedGraph = liftedModule.resolveNamesModular

    // Capture -> imported f_0 is no longer referenced by local variable!
    assert(!liftedGraph.E.exists(_._2.contains(f_0base)))

    val fixedModule = NameFix.nameFixModular(module3.resolveNamesModular, liftedModule, Set(moduleBase.interface))
    val fixedGraph = fixedModule.resolveNamesModular

    // Capture solved
    assert(fixedGraph.E.exists(_._2.contains(f_0base)))

    // NameFix could not avoid renaming exported identifiers but avoided renaming imported names
    assert(fixedGraph.I == liftedGraph.I)
    info(fixedModule.toString)
  }
}
