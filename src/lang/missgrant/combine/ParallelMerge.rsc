module lang::missgrant::combine::ParallelMerge

import lang::missgrant::extract::ToRelation;
import lang::missgrant::ast::MissGrant;

// we assume the eventnames in ctl1 and ctl2 are equal if their tokens are equal
// this way, the eventnames can be used as the alphabet. Same for actions.
// the controllers must also both be deterministic
public Controller parMerge(Controller ctl1, Controller ctl2) {
  tr = parMergeTransRel(transRel(ctl1), transRel(ctl2));
  return controller(unique(ctl1.events + ctl2.events),
  			unique(ctl1.resets + ctl2.resets),
  			unique(ctl1.commands + ctl2.commands),
  			lift(tr, actionMap(ctl1), actionMap(ctl2)));
}

public TransRel[tuple[str,str]] parMergeTransRel(TransRel[str] tr1, TransRel[str] tr2) {
  states = tr1<0> + tr1<2> + tr2<0> + tr2<2>;
  tr = {};
  for (s1 <- states, s2 <- states) {
    tr += { <<s1, s2>, e, <u1, u2>> | <s1, e, u1> <- tr1, <s2, e, u2> <- tr2 }; 
    tr += { <<s1, s2>, e, <u1, s2>> | <s1, e, u1> <- tr1, !any(<s2, e, _> <- tr2) };
    tr += { <<s1, s2>, e, <s1, u2>> | !any(<s1, e, _> <- tr1), <s2, e, u2> <- tr2  };
  }
  return tr;
}

public list[State] lift(TransRel[tuple[str,str]] tr, ActionMap am1, ActionMap am2) {
  return for (s:<a, b> <- tr<0>) {
    append state(a + b, unique(am1[a] + am2[b]), 
    	[ transition(e, c + d) | <s, e, <c, d>> <- tr ]);
  }
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