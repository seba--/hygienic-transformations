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

