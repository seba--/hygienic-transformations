module lang::missgrant::extract::ToRelation

import  lang::missgrant::ast::MissGrant;

alias ActionMap = map[str state, list[str] actions];
alias TransRel[&State] = rel[&State from, str event, &State to]; 

public TransRel[str] transRel(Controller ctl) {
  return { <s1, e, s2> | /state(s1, _, ts) <- ctl, transition(e, s2) <- ts };
}

public ActionMap actionMap(Controller ctl) {
  return ( s: as | /state(s, as, _) <- ctl ); 
}