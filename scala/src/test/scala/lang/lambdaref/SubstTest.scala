package lang.lambdaref

import lang.lambdaref._
import org.scalatest._

/**
 * Created by seba on 01/08/14.
 */
class SubstTest extends FunSuite {


  val p1 = {
    val ref1 = Var()
    val t = Lam("x", ref1)
    ref1.initialize(t)
    t
  }

  test ("p1") {
    assertResult(p1)(p1.substGraph("x", new Var()))
    assertResult(p1)(p1.substGraph("y", new Var()))
  }

  val p2 = {
    val ref1 = Var()
    val t = Lam("y", Lam("x", ref1))
    ref1.initialize(t)
    t
  }
  test ("p2") {
    assertResult(p2)(p2.substGraph("x", Var()))
    assertResult(p2)(p2.substGraph("y", Var()))
  }
}
