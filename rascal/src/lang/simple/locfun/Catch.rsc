module lang::simple::locfun::Catch

extend lang::simple::locfun::Locfun;
import lang::simple::inline::Subst;

Prog desugar(Prog p) = visit (p) { case Exp e => desugar(e) };
Prog desugar0(Prog p) = visit (p) { case Exp e => desugar0(e) };

Exp desugar0(\catch(Exp e)) 
  = let(fdef("foo", ["throw"], e),
      call("callcc", [var("foo")]));

default Exp desugar0(Exp e) = e;

Exp desugar(\catch(Exp e)) 
  = let(fdef("foo", ["throw_"], e2),
      call("callcc", [var("foo")]))
   when e2 := substExp(e, "throw", var("throw_"));

default Exp desugar(Exp e) = e;
   
