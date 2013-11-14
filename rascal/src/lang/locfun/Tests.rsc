module lang::locfun::Tests


import lang::simple::Syntax;
import lang::locfun::Syntax;
import lang::simple::Parse;
import lang::simple::Implode;
import lang::simple::AST;
import lang::locfun::AST;
import lang::locfun::Decorate;
import lang::locfun::Abstract;
import lang::locfun::Lift;


Exp exp1 = block([vdef("y", val(nat(1)))],
                 block([fdef("f", ["x"], call("f", [var("y")]))], call("f", [val(nat(1))])));

Prog prog1 = prog([], [exp1]);

Prog testExp1() = lift(prog1);
