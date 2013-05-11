module lang::missgrant::base::Merge

import lang::missgrant::base::AST;
import IO;
import List;
import Set;


// we assume the eventnames in ctl1 and ctl2 are equal if their tokens are equal
// this way, the eventnames can be used as the alphabet. Same for actions.
// the controllers must also both be deterministic


Controller merge(Controller ctl1, Controller ctl2) =
  controller(dup(ctl1.events + ctl2.events),
	    unique(ctl1.resets + ctl2.resets),
	    unique(ctl1.commands + ctl2.commands),
		    mergeStates(ctl1, ctl2));

private list[State] mergeStates(Controller ctl1, Controller ctl2) {
  memo = {};
  states = [];
  env1 = ( s.name: s | s <- ctl1.states); 
  env2 = ( s.name: s | s <- ctl2.states);
  
  str merge(State s1, State s2) {
	    nn = "<s1.name>__<s2.name>";
	  
	    if (nn in memo)
	      return nn;
	    memo += {nn};
	
	    e1 = [ e | transition(e, _) <- s1.transitions ];
	    e2 = [ e | transition(e, _) <- s2.transitions ];
	    both = e1 & e2;
	  
	    trs = [ transition(e, merge(env1[u1], env2[u2])) | e <- both, 
	  		          transition(e, u1) <- s1.transitions, 
	  		          transition(e, u2) <- s2.transitions ]
	        + [ transition(e, merge(env1[u1], s2)) | e <- e1 - both, 
	      		      transition(e, u1) <- s1.transitions ] 
	        + [ transition(e, merge(s1, env2[u2])) | e <- e2 - both, 
	      		      transition(e, u2) <- s2.transitions ];
	
	    states = [state(nn, dup(s1.actions + s2.actions), trs)] + states;
	    return nn;
  }
  
  State initial(Controller ctl) = ctl.states[0];
  
  merge(initial(ctl1), initial(ctl2));
  
  return states;
}

list[&T] unique(list[&T] lst) = toList(toSet(lst));

