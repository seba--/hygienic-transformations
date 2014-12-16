package lang.lambda

import lang.lambda.let._
import lang.lambda.num._
import name.namefix.NameFix
import name.{Edges, NameGraph}
import org.scalatest._

/**
* Created by seba on 01/08/14.
*/
class NamefixTestNested extends FunSuite {

  val fixer: NameFix = NameFix.fixer

  val p =
    Let("x", Num(1),
      Add(
        Var("x"),
        Let("x", Num(1),
          Add(
          Var("x"),
          Let("x", Num(1), Var("x"))))))

  def x1def(p: Exp) = p match {case Let(x,_,_) => x}
  def x1use(p: Exp) = p match {case Let(_,_,Add(Var(x),_)) => x}
  def x2def(p: Exp) = p match {case Let(_,_,Add(_,Let(x,_,_))) => x}
  def x2use(p: Exp) = p match {case Let(_,_,Add(_,Let(_,_,Add(Var(x),_)))) => x}
  def x3def(p: Exp) = p match {case Let(_,_,Add(_,Let(_,_,Add(_,Let(x,_,_))))) => x}
  def x3use(p: Exp) = p match {case Let(_,_,Add(_,Let(_,_,Add(_,Let(_,_,Var(x)))))) => x}

  test ("fix nested 1") {
    val gs = NameGraph(Set(x3def(p).id, x3use(p).id), Map(x3use(p).id -> x3def(p).id))
    val fixed = fixer.nameFix(gs, p)
    val g: NameGraph = fixed.resolveNames

    assert (g.E(x1use(fixed).id) == x1def(fixed).id)
    assert (g.E(x2use(fixed).id) == x2def(fixed).id)
    assert (g.E(x3use(fixed).id) == x3def(fixed).id)
  }

  test ("fix nested 2") {
    val gs = NameGraph(Set(x2def(p).id, x3use(p).id), Map(x3use(p).id -> x2def(p).id))
    val fixed = fixer.nameFix(gs, p)
    val g: NameGraph = fixed.resolveNames

    assert (g.E(x1use(fixed).id) == x1def(fixed).id)
    assert (g.E(x2use(fixed).id) == x1def(fixed).id)
    assert (g.E(x3use(fixed).id) == x2def(fixed).id)
  }

  test ("fix nested 3") {
    val gs = NameGraph(Set(x1def(p).id, x3use(p).id), Map(x3use(p).id -> x1def(p).id))
    val fixed = fixer.nameFix(gs, p)
    val g: NameGraph = fixed.resolveNames
    
    assert (!g.E.isDefinedAt(x1use(fixed).id))
    assert (g.E(x2use(fixed).id) == x2def(fixed).id)
    assert (g.E(x3use(fixed).id) == x1def(fixed).id)
  }

  test ("fix nested 4") {
    val gs = NameGraph(Set(x1def(p).id, x2use(p).id), Map(x2use(p).id -> x1def(p).id))
    val fixed = fixer.nameFix(gs, p)
    val g: NameGraph = fixed.resolveNames

    assert (!g.E.isDefinedAt(x1use(fixed).id))
    assert (g.E(x2use(fixed).id) == x1def(fixed).id)
    assert (g.E(x3use(fixed).id) == x3def(fixed).id)
  }

  test ("fix nested 5") {
    val gs = NameGraph(Set(x2def(p).id), Map() : Edges)
    val fixed = fixer.nameFix(gs, p)
    val g: NameGraph = fixed.resolveNames

    assert (g.E(x1use(fixed).id) == x1def(fixed).id)
    assert (g.E(x2use(fixed).id) == x1def(fixed).id)
    assert (g.E(x3use(fixed).id) == x3def(fixed).id)
  }

  test ("fix nested 6") {
    val gs = NameGraph(Set(x2def(p).id, x2use(p).id, x3def(p).id, x3use(p).id), Map(x3use(p).id -> x2def(p).id))
    val fixed = fixer.nameFix(gs, p)
    val g: NameGraph = fixed.resolveNames

    assert (g.E(x1use(fixed).id) == x1def(fixed).id)
    assert (!g.E.isDefinedAt(x2use(fixed).id))
    assert (g.E(x3use(fixed).id) == x2def(fixed).id)
  }

  test ("fix nested 7") {
    val gs = NameGraph(Set(x2use(p).id, x3def(p).id), Map(x2use(p).id -> x3def(p).id))
    val fixed = fixer.nameFix(gs, p)
    val g: NameGraph = fixed.resolveNames

    assert (g.E(x1use(fixed).id) == x1def(fixed).id)
    assert (!g.E.isDefinedAt(x2use(fixed).id))
    assert (g.E(x3use(fixed).id) == x2def(fixed).id)
  }

  test ("fix nested 8") {
    val gs = NameGraph(Set(x2use(p).id, x3def(p).id, x1def(p).id), Map(x2use(p).id -> x3def(p).id))
    val fixed = fixer.nameFix(gs, p)
    val g: NameGraph = fixed.resolveNames

    assert (!g.E.isDefinedAt(x1use(fixed).id))
    assert (!g.E.isDefinedAt(x2use(fixed).id))
    assert (g.E(x3use(fixed).id) == x2def(fixed).id)
  }

  test ("fix nested 9") {
    val gs = NameGraph(Set(x2use(p).id, x3def(p).id, x1def(p).id, x3use(p).id), Map(x2use(p).id -> x3def(p).id))
    val fixed = fixer.nameFix(gs, p)
    val g: NameGraph = fixed.resolveNames

    assert (!g.E.isDefinedAt(x1use(fixed).id))
    assert (!g.E.isDefinedAt(x2use(fixed).id))
    assert (!g.E.isDefinedAt(x3use(fixed).id))
  }

  test ("fix nested 10") {
    val gs = NameGraph(Set(x1use(p).id, x3def(p).id), Map(x1use(p).id -> x3def(p).id))
    val fixed = fixer.nameFix(gs, p)
    val g: NameGraph = fixed.resolveNames

    assert (!g.E.isDefinedAt(x1use(fixed).id))
    assert (g.E(x2use(fixed).id) == x2def(fixed).id)
    assert (g.E(x3use(fixed).id) == x2def(fixed).id)
  }

  // requires three consecutive renamings (recursive calls of fix)
  test ("fix nested 11") {
    val gs = NameGraph(Set(x2def(p).id, x2use(p).id, x3def(p).id, x3use(p).id), Map(x2use(p).id -> x2def(p).id))
    val fixed = fixer.nameFix(gs, p)
    val g: NameGraph = fixed.resolveNames

    assert (g.E(x1use(fixed).id) == x1def(fixed).id)
    assert (g.E(x2use(fixed).id) == x2def(fixed).id)
    assert (!g.E.isDefinedAt(x3use(fixed).id))
  }
}
