module lang::missgrant::extract::ToRelation

import  lang::missgrant::ast::MissGrant;

alias TransRel = rel[str state, str eventToken,  str toState];
alias ActionRel = rel[str state, str commandToken];


public TransRel transRel(Controller ctl) {
  return { <s1, e, s2> | /state(s1, _, ts) <- ctl, transition(e, s2) <- ts };
}

public ActionRel commands(Controller ctl) {
  return {<s, a> | /state(s, as, _) <- ctl, a <- as }; 
}