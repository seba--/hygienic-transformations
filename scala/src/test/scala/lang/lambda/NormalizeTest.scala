package lang.lambda

import org.scalatest._

/**
 * Created by seba on 01/08/14.
 */
class NormalizeTest extends FunSuite {

  val p1 = Lam("x", Var("x"))
  test ("p1") { assertResult(p1)(p1.unsafeNormalize) }

  val p2 = App(Lam("x", Var("x")), Var("y"))
  test ("p2") { assertResult(Var("y"))(p2.unsafeNormalize) }

  val p3 = App(Lam("x", Lam("y", App(Var("x"), Var("y")))), Var("y"))
  test ("p3_capture") { assertResult(Lam("y", App(Var("y"), Var("y"))))(p3.unsafeNormalize) }
  test ("p3_safe") { assertResult(Lam("y_0", App(Var("y"), Var("y_0"))))(p3.normalize) }

}
