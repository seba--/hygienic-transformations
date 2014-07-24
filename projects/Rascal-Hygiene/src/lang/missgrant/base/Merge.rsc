module lang::missgrant::base::Merge

import lang::missgrant::base::AST;
import lang::missgrant::base::Implode;
import lang::missgrant::base::NameRel;
import lang::missgrant::base::Unparse;
import IO;
import List;
import Set;
import name::IDs;
import name::NameGraph;
import name::HygienicCorrectness;



// we assume the eventnames in ctl1 and ctl2 are equal if their tokens are equal
// this way, the eventnames can be used as the alphabet. Same for actions.
// the controllers must also both be deterministic


Controller merge(Controller ctl1, Controller ctl2, NameGraph g1, NameGraph g2) =
  controller(dup(ctl1.events + ctl2.events),
	    unique(ctl1.resets + ctl2.resets),
	    unique(ctl1.commands + ctl2.commands),
		    mergeStates(ctl1, ctl2, g1, g2));

list[State] mergeStates(Controller ctl1, Controller ctl2, NameGraph g1, NameGraph g2) {
  memo = ();
  states = [];
  env1 = ( s.name: s | s <- ctl1.states );
  env2 = ( s.name: s | s <- ctl2.states );
  
  str merge(ID s1, ID s2) {
	    nn = "<nameOf(s1, g1)>__<nameOf(s2, g2)>";
	    key = <s1, s2>;
	    if (key in memo) {
	      return nn;
	    }
	    memo[key] = nn;
	  
	    st1 = env1[nameOf(s1, g1)];
	    st2 = env2[nameOf(s2, g2)];
	    
	    e1 = [ e | transition(e, _) <- st1.transitions ];
	    e2 = [ e | transition(e, _) <- st2.transitions ];
	    both = e1 & e2;
	  
	    trs = [ transition(e, merge(refOf(u1, g1), refOf(u2, g2))) | e <- both, 
	  		          transition(e, u1) <- st1.transitions, 
	  		          transition(e, u2) <- st2.transitions ]
	        + [ transition(e, merge(refOf(u1, g1), s2)) | e <- e1 - both, 
	      		      transition(e, u1) <- st1.transitions ] 
	        + [ transition(e, merge(s1, refOf(u2, g2))) | e <- e2 - both, 
	      		      transition(e, u2) <- st2.transitions ];
	
	    states = [state(nn, dup(st1.actions + st2.actions), trs)] + states;
	    return nn;
  }
  
  State initial(Controller ctl) = ctl.states[0];
  
  merge(getID(initial(ctl1).name), getID(initial(ctl2).name));
  
  return states;
}

list[&T] unique(list[&T] lst) = toList(toSet(lst));

void testIt() {
  c1 = load(|project://Rascal-Hygiene/input/merge1.ctl|);
  c2 = load(|project://Rascal-Hygiene/input/merge2.ctl|);
  m = merge(c1, c2, resolveNames(c1), resolveNames(c2));
  println(unparse(m));
  ng1 = union(resolveNames(c1), resolveNames(c2));
  ng2 = resolveNames(m);
  println(isHygienic(ng1, ng2)); 
}

