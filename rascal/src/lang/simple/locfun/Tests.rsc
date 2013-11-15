module lang::simple::locfun::Tests

import lang::simple::AST;
import lang::simple::Pretty;
import lang::simple::locfun::Locfun;

import name::HygienicCorrectness;
import name::Rename;

import IO;

Exp exp1 = block([vdef("y", val(nat(1)))],
                 block(fdef("f", ["x"], call("f", [plus(var("x"),var("y"))])), call("f", [val(nat(1))])));

Prog prog1 = prog([], [exp1]);

Prog testExp1() = desugarLocfun(prog1);

Prog trans1() = testExp1();
test bool test1() {
  Gs = resolveNames(prog1);
  prog2 = testExp1();
  Gt = resolveNames(prog2);
  iprintln(Gs);
  iprintln(Gt);
  iprintln(unhygienicLinks(Gs,Gt));
  return isCompiledHygienically(Gs, Gt);
}

Prog transFixed1() {
  Gs = resolveNames(prog1);
  prog2 = fixHygiene(Gs, trans1(), resolveNames);
}
test bool testFixed1() {
  Gs = resolveNames(prog1);
  transformed = testExp1();
  fixed = fixHygiene(Gs, transformed, resolveNames);
  return transformed == fixed;
}