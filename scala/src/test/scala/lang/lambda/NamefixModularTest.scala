package lang.lambda

import lang.lambda.module.Module
import lang.lambda.num._
import name.Identifier
import name.namefix.NameFix
import org.scalatest._

class NamefixModularTest extends FunSuite {
  val moduleBase = Module("base", Set(), Map(
    ("times2", (Lam("x", Add(Var("x"), Var("x"))), false)),
    ("times3", (Lam("x", Add(App(Var("times2"), Var("x")), Var("x"))), true))))
  val moduleBaseEmpty = Module(moduleBase.name, Set(), Map())
  val moduleBase2Orig = Module("base2", Set(), Map())
  val moduleBase2Synth = Module(moduleBase2Orig.name, Set(), Map(
    ("times3", (Lam("x", Add(Var("x"), Var("x"))), true))))
  val calcOriginal = (Identifier("calc"), (App(Var("times3"), Num(5)), true))
  val times3Synth = (Identifier("times3"), (Add(Num(3), Add(Num(3), Num(3))), true))
  val moduleClient = Module("client", Set("times3"), Map(calcOriginal))
  val moduleClientTransformed = Module(moduleClient.name, moduleClient.imports, Map(calcOriginal, times3Synth))

  val times2Def = moduleBase.defs.keys.find(_.name == "times2").get
  val times2Ref = moduleBase.defs.find(_._1.name == "times3").get._2._1 match {
    case Lam(_, Add(App(Var(x), _), _)) => x
  }
  val times3DefSynth = times3Synth._1
  val times3Import = moduleClient.imports.find(_.name == "times3").get
  val times3Ref = calcOriginal._2._1 match {
    case App(Var(x), _) => x
  }
  val calcDef = calcOriginal._1

  val moduleBaseInterface = moduleBase.interface
  val moduleBaseEmptyInterface = moduleBaseEmpty.interface
  val moduleBase2OrigInterface = moduleBase2Orig.interface
  val moduleBase2SynthInterface = moduleBase2Synth.interface
  val moduleClientInterface = moduleClient.interface

  test ("internal capture in transformed graph") {
    moduleClient.link(moduleBaseInterface)
    moduleClientTransformed.link(moduleBaseInterface)
    val originalGraph = moduleClient.resolveNamesModular
    val transformedGraph = moduleClientTransformed.resolveNamesModular
    assert(originalGraph.E(times3Ref).contains(times3Import))
    assert(transformedGraph.E(times3Ref).contains(times3DefSynth))
  }

  test ("name-fix of internal capture") {
    moduleClient.link(moduleBaseInterface)
    moduleClientTransformed.link(moduleBaseInterface)
    val originalGraph = moduleClient.resolveNamesModular
    val transformedGraph = moduleClientTransformed.resolveNamesModular
    val fixedModule = NameFix.nameFixModular(originalGraph, moduleClientTransformed, Set(moduleBaseInterface))
    val fixedGraph = fixedModule.resolveNamesModular
    assert(fixedGraph.E(times3Ref).contains(times3Import))
  }

  test ("name-fix of external capture") {
    moduleClient.link(Set(moduleBaseEmptyInterface, moduleBase2OrigInterface))
    val originalGraph = moduleClient.resolveNamesModular
    moduleClient.link(Set(moduleBaseEmptyInterface, moduleBase2SynthInterface))
    val transformedGraph = moduleClient.resolveNamesModular

    val fixedModule = NameFix.nameFixModular(originalGraph, moduleClient, Set(moduleBaseEmptyInterface, moduleBase2SynthInterface))
    val fixedGraph = fixedModule.resolveNamesModular

    assert(!fixedGraph.E.contains(times3Import))
  }

  test ("name-fix of unsolvable capture") {
    moduleClient.link(Set(moduleBaseInterface, moduleBase2OrigInterface))
    val originalGraph = moduleClient.resolveNamesModular
    moduleClient.link(Set(moduleBaseInterface, moduleBase2SynthInterface))
    val transformedGraph = moduleClient.resolveNamesModular

    intercept[RuntimeException] {
      val fixedModule = NameFix.nameFixModular(originalGraph, moduleClient, Set(moduleBaseInterface, moduleBase2SynthInterface))
    }
  }
}
