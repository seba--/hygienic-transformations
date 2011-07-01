module lang::missgrant::combine::ParallelMerge

import lang::missgrant::extract::ToRelation;
import lang::missgrant::ast::MissGrant;
import IO;


// we assume the eventnames in ctl1 and ctl2 are equal if their tokens are equal
// this way, the eventnames can be used as the alphabet. Same for actions.
// the controllers must also both be deterministic


public Controller parMerge(Controller ctl1, Controller ctl2) {
   states = [];
   void add(State s) { states += [s]; }
   parMerge(ctl1, ctl2, add); 
   return controller(unique(ctl1.events + ctl2.events),
  			unique(ctl1.resets + ctl2.resets),
  			unique(ctl1.commands + ctl2.commands),
  			states);
}

private str parMerge(Controller ctl1, Controller ctl2, void(State) add) {
  memo = {};
  env1 = stateEnv(ctl1);
  env2 = stateEnv(ctl2);
  
  str merge(State s1, State s2) {
	  nn = newName(s1, s2);
	  
	  if (nn in memo)
	    return nn;
	  memo += {nn};
	
	  e1 = consumes(s1);
	  e2 = consumes(s2);
	  both = e1 & e2;
	  
	  trs = [ transition(e, merge(env1[u1], env2[u2])) | e <- both, 
	  		  transition(e, u1) <- s1.transitions, 
	  		  transition(e, u2) <- s2.transitions ]
	      + [ transition(e, merge(env1[u1], s2)) | e <- e1 - both, transition(e, u1) <- s1.transitions ] 
	      + [ transition(e, merge(s1, env2[u2])) | e <- e2 - both, transition(e, u2) <- s2.transitions ];
	
	  add(state(nn, unique(s1.actions + s2.actions), trs));
	  return nn;
  }
  
  return merge(initial(ctl1), initial(ctl2));
}


private list[&T] unique(list[&T] l) {
  ul = [];
  for (x <- l) {
    if (x notin ul) {
      ul += [x];
    }
  }
  return ul;
}

private str newName(State s1, State s2) {
  return "<s1.name>$<s2.name>";
}


