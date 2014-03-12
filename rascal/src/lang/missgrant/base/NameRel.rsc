module lang::missgrant::base::NameRel

import lang::missgrant::base::AST;

import name::Relation;
import name::IDs;


map[str,ID] collectStates(Controller ctl) =
  ( s.name: getID(s.name) | /State s := ctl );

NameGraph relateStates(Controller ctl, map[str,ID] stateDefs) {
  states = stateDefs<0,1>;
  transitionNames = { <t.state, getID(t.state)> | /Transition t := ctl };
  rels = { <getID(t.state), stateDefs[t.state]> | /Transition t := ctl };
  return makeGraph(states + transitionNames, rels);
}

NameGraph resolveNames(Controller ctl) =
  relateStates(ctl, collectStates(ctl));