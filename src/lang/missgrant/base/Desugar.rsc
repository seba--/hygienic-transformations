module lang::missgrant::base::Desugar

import lang::missgrant::base::AST;
import IO;

data Desugaring
  = resetEvents()
  ;

Controller desugar(list[Desugaring] features, Controller ctl) 
  = ( ctl | desugar(f, it) | Desugaring f <- features ); 

Controller desugar(resetEvents(), Controller ctl) {
  init = ctl.states[0].name;
  ctl = visit (ctl) {
    case s:state(n, as, ts) => state(n, as, ts + [ transition(e, init) | e <- ctl.resets ])[@location=s@location]
  };
  ctl.resets = [];
  return ctl;
}


  