package lang.lambdaref

import org.scalatest._

class NormalizeTest extends FunSuite {

  val p1 = Lam("x", x => Var(x))
  test ("p1") { assertResult(p1)(p1.normalizeGraph) }
  test ("p1-unsafe") { assertResult(p1)(p1.unsafeNormalize) }

  val refy = Var("y")
  val p2 = App(Lam("x", x => Var(x)), refy)
  test ("p2") { assertResult(refy)(p2.normalizeGraph) }
  test ("p2-unsafe") { assertResult(refy)(p2.unsafeNormalize) } // still safe

  val refy2 = Var("y")
  val p3 = App(Lam("x", x => Lam("y", y => App(Var(x), Var(y)))), refy2)
  test ("p3") {
    val dy = Lam("y", y => App(refy2, Var(y)))
    assert(refy2._target.isEmpty)
    assertResult(dy)(p3.normalizeGraph)
  }
  test ("p3-unsafe") {
    val dy = Lam("y", y => App(Var(y), Var(y))) // captured refy2
    assert(refy2._target.isEmpty)
    assertResult(dy)(p3.unsafeNormalize)
  }
}
