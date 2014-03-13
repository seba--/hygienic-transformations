module lang::simple::locfun::Catch

extend lang::simple::locfun::Locfun;
import lang::simple::inline::Subst;
import String;

Prog desugar(Prog p) = visit (p) { case Exp e => desugar(e) };
Prog desugar0(Prog p) = visit (p) { case Exp e => desugar0(e) };

Exp desugar0(\catch(Exp e)) 
  = let(fdef("throw", [], call("callcc", [])), e);

default Exp desugar0(Exp e) = e;

Exp desugar(\catch(Exp e)) 
  = let(fdef("throw", [], call("callcc", [])), 
       allowCapture("throw", e));

default Exp desugar(Exp e) = e;

&T allowCapture(str name, &T t) {
  return visit (t) {
    case str name2 => tagString(name2, "synth", "true")
      when name2 == name
  }
}


//Exp desugar(\catch(Exp e)) 
//  = let(fdef("foo", ["throw_"], e2),
//      call("callcc", [var("foo")]))
//   when e2 := 
//    visit (e1) { case call("throw", args) => call(
//
//default Exp desugar(Exp e) = e;
//   
