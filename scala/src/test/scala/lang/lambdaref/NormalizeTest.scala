package lang.lambdaref

import org.scalatest._

class NormalizeTest extends FunSuite {

  val p1 = {
    val ref = Var()
    val t = Lam("x", ref)
    ref.initialize(t)
    t
  }
  test ("p1") { assert(p1 equiv p1.normalizeGraph) }

  val refy = Var()
  val p2 = {
    val refx = Var()
    val decx = Lam("x", refx)
    refx.initialize(decx)
    App(decx, refy)
  }
  test ("p2") { assert(refy equiv p2.normalizeGraph) }

  val refy2 = Var()
  val p3 = {
    val refx = Var()
    val refy1 = Var()
    val decy1 = Lam("y", App(refx, refy1))
    val decx = Lam("x", decy1)
    refy1.initialize(decy1)
    refx.initialize(decx)
    App(decx, refy2)
  }
  test ("p3") {
    val ry = Var()
    val dy = Lam("y", App(refy2, ry))
    ry.initialize(dy)
    assert(refy2._target.isEmpty)
    assert(dy equiv p3.normalizeGraph)
  }
}
