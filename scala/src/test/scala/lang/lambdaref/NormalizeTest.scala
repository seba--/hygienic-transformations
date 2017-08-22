package lang.lambdaref

import org.scalatest._

class NormalizeTest extends FunSuite {

  val p1 = Lam("x", x => Var(x))
  test ("p1") { assertResult(p1)(p1.normalizeGraph) }
  test ("p1-unsafe") { assertResult(p1)(p1.unsafeNormalize) }

  val refy = Var()
  val p2 = App(Lam("x", x => Var(x)), refy)
  test ("p2") { assertResult(refy)(p2.normalizeGraph) }
  test ("p2-unsafe") { assertResult(refy)(p2.unsafeNormalize) }

  val refy2 = Var()
  val p3 = App(Lam("x", x => Lam("y", y => App(Var(x), Var(y)))), refy2)
  test ("p3") {
    val ry = Var()
    val dy = Lam("y", App(refy2, ry))
    ry.initialize(dy)
    assert(refy2._target.isEmpty)
    assertResult(dy)(p3.normalizeGraph)
  }
}
