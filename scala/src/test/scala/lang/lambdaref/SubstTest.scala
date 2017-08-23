package lang.lambdaref

import lang.lambdaref._
import org.scalatest._
import ref.Structural

/**
 * Created by seba on 01/08/14.
 */
class SubstTest extends FunSuite {

  def assertResult(expected: Structural)(actual: Structural) = super.assert(expected equiv actual)

  val p1 = Lam("x", x => Var(x))
  test ("p1") {
    assertResult(p1)(p1.substGraph("x", Var("z")))
    assertResult(p1)(p1.substGraph("y", Var("z")))
  }

  val p2 = Lam("y", y => Lam("x", x => Var(y)))
  test ("p2") {
    assertResult(p2)(p2.substGraph("x", Var("z")))
    assertResult(p2)(p2.substGraph("y", Var("z")))
  }
}
