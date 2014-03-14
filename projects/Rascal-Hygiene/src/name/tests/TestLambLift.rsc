module name::tests::TestLambLift

import lang::simple::AST;
import lang::simple::Pretty;
import lang::simple::locfun::Locfun;

import name::HygienicCorrectness;
import name::NameFix;

import IO;


private Exp liftExp1() =
  let("y", val(nat(1)),
        let(fdef("f", ["x"], call("f", [plus(var("x"),var("y"))])),
              call("f", [val(nat(1))])));

private Prog liftProg1 = prog([], [liftExp1()]);

/* prog1 before local functions being lifted
{
  var y = 1;
  {
    fun f(x) = f(x + y);
    f(1)
  }
}
*/

private Prog liftedProg1() = liftLocfun(liftProg1, resolveNames(liftProg1));

/* prog1 after local functions being lifted
fun f(x, y) = f(x + y, y);

{
  var y = 1;
  f(1, y);
}
*/

test bool testLift1() {
  Gs = resolveNames(liftProg1);
  Gt = resolveNames(liftedProg1());
  return !isCompiledHygienically(Gs, Gt);
}


test bool testLiftFixed1() {
  Gs = resolveNames(liftProg1);
  lp = liftedProg1();
  println("Lifted:\n<pretty(lp)>");
  fixedProg1 = nameFix(#Prog, Gs, lp, resolveNames);
  Gt = resolveNames(fixedProg1);
  println("Fixed:\n<pretty(fixedProg1)>");
  return isCompiledHygienically(Gs, Gt);
}


private Prog liftProg2 =
  prog([fdef("f", ["x"], plus(var("x"), val(nat(1))))],
       [let("y", call("f", [val(nat(1))]),
              let(fdef("f", ["x"], call("f", [plus(var("x"),var("y"))])),
                    call("f", [val(nat(1))])))]);
private Prog theProg2() = liftProg2;

/* prog2 before local functions being lifted
fun f(x) = x + 1;

{
  var y = f(1);
  {
    fun f(x) = f(x + y);
    f(1)
  }
}
*/

private Prog liftedProg2() = liftLocfun(liftProg2, resolveNames(liftProg2));

/* prog2 after local functions being lifted
fun f(x) = x + 1;

fun f(x, y) = f(x + y, y);

{
  var y = f(1);
  f(1, y);
}
*/

test bool testLift2() {
  Gs = resolveNames(liftProg2);
  lp = liftedProg2();
  Gt = resolveNames(lp);
  //iprintln(Gs);
  //iprintln(Gt);
  return !isCompiledHygienically(Gs, Gt);
}

private Prog liftFixProg2() {
  Gs = resolveNames(liftProg2);
  return nameFix(#Prog, Gs, liftedProg2(), resolveNames);
}

test bool testLiftFixed2() {
  Gs = resolveNames(liftProg2);
  lp = liftedProg2();
  println("Lifted:\n<pretty(lp)>");
  fixedProg2 = nameFix(#Prog, Gs, lp, resolveNames);
  println("Fixed:\n<pretty(fixedProg2)>");
  Gt = resolveNames(fixedProg2);
  return isCompiledHygienically(Gs, Gt);
}



private Exp exp3 =
  let("y", call("f", [val(nat(10))]),
        let(fdef("f", ["x"], call("f", [plus(var("x"),var("y"))])),
            let(fdef("g", ["x"], call("f", [plus(var("y"), plus(var("x"), val(nat(1))))])),
               plus(
                call("f", [val(nat(1))]),
                call("g", [val(nat(3))]))
               )));

private Prog prog3 = prog([fdef("f", ["x"], plus(var("x"), val(nat(1))))], [exp3]);


/* prog3 before local functions being lifted
fun f(x) = x + 1;
{
  var y = f(10);
  {
    fun f(x) = f(x + y);
    { fun g(x) = f(y + x + 1);
      f(1) + g(3)
    }
  }
}
*/

private Prog liftProg3() = liftLocfun(prog3, resolveNames(prog3));


test bool test3() {
  Gs = resolveNames(prog3);
  liftedProg3 = liftProg3();
  Gt = resolveNames(liftedProg3);
  //iprintln(Gs);
  //iprintln(Gt);
  return !isCompiledHygienically(Gs, Gt);
}

private Prog liftFixProg3() {
  Gs = resolveNames(prog3);
  return nameFix(#Prog, Gs, liftProg3(), resolveNames);
}

test bool testFixed3() {
  Gs = resolveNames(prog3);
  liftedProg3 = liftProg3();
  println("Lifted:\n<pretty(liftedProg3)>");
  fixedProg3 = nameFix(#Prog, Gs, liftedProg3, resolveNames);
  println("Fixed:\n<pretty(fixedProg3)>");
  Gt = resolveNames(fixedProg3);
  return isCompiledHygienically(Gs, Gt);
}

