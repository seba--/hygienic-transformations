module lang::missgrant::base::Desugar

import lang::missgrant::base::AST;
import IO;
import List;

data Desugaring
  = resetEvents()
  | retries()
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

Controller desugar(retries(), Controller ctl) {
  // We can't use visit() because it seems to work on a copy (?)
  // so the states added in the first pass don't get visited in
  // the next. Manually loop until the fixpoint.
  
  // As long as we have a 2-or-higher retry-transition somewhere in the controller
  while ([s1*, s:state(name, as, [t1*, transition(n, event, failState), t2*]), s2*] := ctl.states, n > 1) {
    // Add a retry state with one fewer retry
    State ss = state("<name>R", as, t1 + t2);
    ss.transitions += [transition(n - 1, event, failState)];
	
	// Replace the matched retry transition with a normal transition to the newly
	// generated state.
	// Note: I'm completely replacing ctl.states because I don't trust *ANY* reference
	// to *ANY* state object anywhere -- they all turn out to be copies which send me
	// into an infinite while() loop.
    ctl.states = s1 + s2 + [ss, state(name, as, t1 + t2 + [transition(event, ss.name)])];
  }
  
  // Finally, use a regular visit() command to replace all remaining 1-retries with regular
  // transitions. Doing this separately comes out a lot nicer than doing case analysis inside
  // the while() loop, and we don't have issues with copies in this case.
  ctl = visit (ctl) {
    case t:transition(1, event, failState) => transition(event, failState)
  }  
  
  return ctl;
}


  