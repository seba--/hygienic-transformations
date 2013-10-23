module lang::missgrant::base::NameRel

import lang::missgrant::base::AST;

import name::Relation;

map[str,loc] collectStates(Controller ctl) =
  ( s.name:s@location | /State s := ctl );

NameGraph relateStates(Controller ctl, map[str,loc] stateDefs) {
  states = stateDefs<0,1>;
  transitionNames = { <t.state, t@location> | /Transition t := ctl };
  rels = { <t@location, stateDefs[t.state]> | /Transition t := ctl };
  return makeGraph(states + transitionNames, rels);
}

NameGraph resolveNames(Controller ctl) =
  relateStates(ctl, collectStates(ctl));