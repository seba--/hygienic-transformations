package lang.lambda

import org.scalatest._

/**
 * Created by seba on 01/08/14.
 */
class AlphaEqualTest extends FunSuite {

  val p1 = Lam("x", Var("x"))
  val p2 = Lam("y", Var("y"))
  val p3 = Lam("x", Var("y"))
  val p4 = Lam("y", Var("x"))

  test ("p1=p2") {
    assert(p1.alphaEqual(p2))
  }
  test ("p1!=p3") {assert(!p1.alphaEqual(p3))}
  test ("p1!=p4") {assert(!p1.alphaEqual(p4))}
  test ("p2!=p3") {assert(!p2.alphaEqual(p3))}
  test ("p2!=p4") {assert(!p2.alphaEqual(p4))}
  test ("p3=p4") {assert(p3.alphaEqual(p4))}

  val p5 = Lam("x", Lam("y", App(Var("x"), Var("y"))))
  val p6 = Lam("y", Lam("x", App(Var("y"), Var("x"))))
  val p7 = Lam("y", Lam("x", App(Var("x"), Var("y"))))

  test ("p5=p6") {assert(p5.alphaEqual(p6))}
  test ("p5!=p7") {assert(!p5.alphaEqual(p7))}
  test ("p6!=p7") {assert(!p6.alphaEqual(p7))}

}
