module lang::simple::locfun::Tests

import lang::simple::AST;
import lang::simple::NameRel;
import lang::simple::locfun::Locfun;

import name::HygienicCorrectness;

import IO;

Exp exp1 = block([vdef("y", val(nat(1)))],
                 block(fdef("f", ["x"], call("f", [plus(var("x"),var("y"))])), call("f", [val(nat(1))])));

Prog prog1 = prog([], [exp1]);

Prog testExp1() = desugarLocfun(prog1);

test bool test1() {
  Gs = resolveNames(prog1);
  Gt = resolveNames(testExp1());
  iprintln(Gs);
  iprintln(Gt);
  <refs,defs,self> = unhygienicLinks(Gs,Gt);
  iprintln(<refs,defs,self>);
  return refs == () && defs == () && self == ();
}