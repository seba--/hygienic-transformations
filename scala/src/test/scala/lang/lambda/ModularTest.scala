package lang.lambda

import lang.lambda.module.Module
import lang.lambda.num._
import org.scalatest._

class ModularTest extends FunSuite {
  val moduleBase = Module("base", Set(), Map(
    ("times2", (Lam("x", Add(Var("x"), Var("x"))), false)),
    ("times3", (Lam("x", Add(App(Var("times2"), Var("x")), Var("x"))), true))))
  val moduleClient = Module("client", Set("times3"), Map(
    ("calc", (App(Var("times3"), Num(5)), true))))

  val moduleBaseBad = Module("base", Set(), Map(
    ("times2", (Lam("x", Add(Var("x"), Var("x"))), false)),
    ("times3", (Lam("x", Add(App(Var("times2"), Var("x")), Var("x"))), true))))
  val moduleClientBad = Module("client", Set("times3", "times3"), Map(
    ("calc", (App(Var("times3"), Num(5)), true)),
    ("calc", (Add(Num(1), Num(2)), false))))


  val times2Def = moduleBase.defs.keys.find(_.name == "times2").get
  val times2Ref = moduleBase.defs.find(_._1.name == "times3").get._2._1 match {
    case Lam(_, Add(App(Var(x), _), _)) => x
  }
  val times3Def = moduleBase.defs.keys.find(_.name == "times3").get
  val times3DefBad = moduleBaseBad.defs.keys.find(_.name == "times3").get
  val times3Import = moduleClient.imports.find(_.name == "times3").get
  val times3ImportsBad = moduleClientBad.imports.filter(_.name == "times3").toList
  val times3ImportBad1 = times3ImportsBad.head
  val times3ImportBad2 = times3ImportsBad(1)
  val times3Ref = moduleClient.defs.find(_._1.name == "calc").get._2._1 match {
    case App(Var(x), _) => x
  }
  val calcDef = moduleClient.defs.keys.find(_.name == "calc").get
  val calcDefBad1 = moduleClientBad.defs.find(d => d._1.name == "calc" && d._2._2).get._1
  val calcDefBad2 = moduleClientBad.defs.find(d => d._1.name == "calc" && !d._2._2).get._1

  val moduleBaseInterface = moduleBase.interface
  val moduleClientInterface = moduleClient.interface
  val moduleBaseBadInterface = moduleBaseBad.interface

  moduleClient.link(moduleBaseInterface)
  moduleClientBad.link(Set(moduleBaseInterface, moduleBaseBadInterface))

  test ("correct interfaces") {
    assert(moduleBaseInterface.exportedDefs.contains(times3Def))
    assert(!moduleBaseInterface.exportedDefs.contains(times2Def))
    assert(moduleClientInterface.exportedDefs.contains(calcDef))
  }

  test ("correct internal binding") {
    val baseGraph = moduleBase.resolveNamesModular
    assert(baseGraph.E(times2Ref).size == 1)
    assert(baseGraph.E(times2Ref).contains(times2Def))
  }

  test ("correct import binding") {
    val clientGraph = moduleClient.resolveNamesModular
    assert(clientGraph.E(times3Ref).size == 1)
    assert(clientGraph.E(times3Ref).contains(times3Import))
  }

  test ("correct external binding") {
    val clientGraph = moduleClient.resolveNamesModular
    assert(clientGraph.E(times3Import).size == 1)
    assert(clientGraph.E(times3Import).contains(times3Def))
  }

  test ("correct def conflict binding") {
    val clientBadGraph = moduleClientBad.resolveNamesModular
    assert(clientBadGraph.E(calcDefBad1).size == 1)
    assert(clientBadGraph.E(calcDefBad1).contains(calcDefBad2))
    assert(clientBadGraph.E(calcDefBad2).size == 1)
    assert(clientBadGraph.E(calcDefBad2).contains(calcDefBad1))
  }

  test ("correct import conflict binding") {
    val clientBadGraph = moduleClientBad.resolveNamesModular
    assert(clientBadGraph.E(times3ImportBad1).contains(times3ImportBad2))
    assert(clientBadGraph.E(times3ImportBad2).contains(times3ImportBad1))
  }

  test ("correct external ambiguity binding") {
    val clientBadGraph = moduleClientBad.resolveNamesModular
    assert(clientBadGraph.E(times3ImportBad1).contains(times3Def))
    assert(clientBadGraph.E(times3ImportBad1).contains(times3DefBad))
    assert(clientBadGraph.E(times3ImportBad2).contains(times3Def))
    assert(clientBadGraph.E(times3ImportBad2).contains(times3DefBad))
  }
}
