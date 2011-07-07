module lang::missgrant::extract::ToRelation

import  lang::missgrant::ast::MissGrant;

alias StateTrans = rel[str state, str eventToken,  str toState];
alias Commands = rel[str state, str commandToken];


public StateTrans transRel(Controller ctl) {
  return { <s1, e, s2> | /state(s1, _, ts) <- ctl, transition(e, s2) <- ts };
}

public Commands commands(Controller ctl) {
  return {<s, a> | /state(s, as, _) <- ctl, a <- as }; 
}