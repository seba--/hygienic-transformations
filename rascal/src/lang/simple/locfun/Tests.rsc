module lang::simple::locfun::Tests

import lang::simple::AST;
import lang::simple::Pretty;
import lang::simple::locfun::Locfun;

import name::HygienicCorrectness;
import name::Rename;

import IO;


Exp exp1 =
  block([vdef("y", val(nat(1)))],
        block(fdef("f", ["x"], call("f", [plus(var("x"),var("y"))])),
              call("f", [val(nat(1))])));

Prog prog1 = prog([], [exp1]);

/* prog1 before local functions being lifted
{
  var y = 1;
  {
    fun f(x) = f(x + y);
    f(1)
  }
}
*/

Prog liftProg1() = liftLocfun(prog1);

/* prog1 after local functions being lifted
fun f(x, y) = f(x + y, y);

{
  var y = 1;
  f(1, y);
}
*/

test bool test1() {
  Gs = resolveNames(prog1);
  liftedProg1 = liftProg1();
  Gt = resolveNames(liftedProg1);
  iprintln(Gs);
  iprintln(Gt);
  iprintln(unhygienicLinks(Gs,Gt));
  return isCompiledHygienically(Gs, Gt);
}

Prog liftFixProg1() {
  Gs = resolveNames(prog1);
  return fixHygiene(#Prog, Gs, liftProg1(), resolveNames);
}

test bool testFixed1() {
  Gs = resolveNames(prog1);
  liftedProg1 = liftProg1();
  fixedProg1 = fixHygiene(#Prog, Gs, liftedProg1, resolveNames);
  return liftedProg1 == fixedProg1;
}


Prog prog2 =
  prog([fdef("f", ["x"], plus(var("x"), val(nat(1))))],
       [block([vdef("y", call("f", [val(nat(1))]))],
              block(fdef("f", ["x"], call("f", [plus(var("x"),var("y"))])),
                    call("f", [val(nat(1))])))]);

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

Prog liftProg2() = liftLocfun(prog2);

/* prog2 after local functions being lifted
fun f(x) = x + 1;

fun f(x, y) = f(x + y, y);

{
  var y = f(1);
  f(1, y);
}
*/

test bool test2() {
  Gs = resolveNames(prog2);
  liftedProg2 = liftProg2();
  Gt = resolveNames(liftedProg2);
  iprintln(Gs);
  iprintln(Gt);
  iprintln(unhygienicLinks(Gs,Gt));
  return !isCompiledHygienically(Gs, Gt);
}

Prog liftFixProg2() {
  Gs = resolveNames(prog2);
  return fixHygiene(#Prog, Gs, liftProg2(), resolveNames);
}

test bool testFixed2() {
  Gs = resolveNames(prog2);
  liftedProg2 = liftProg2();
  fixedProg2 = fixHygiene(#Prog, Gs, liftedProg2, resolveNames);
  return liftedProg2 != fixedProg2;
}

