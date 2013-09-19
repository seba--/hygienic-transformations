module lang::missgrant::base::NameRel

import lang::missgrant::base::AST;

map[str,loc] collectStates(Controller ctl) =
  ( s.name:s@location | /State s := ctl );

rel[str name, loc use, loc def] relateStates(Controller ctl, map[str,loc] stateDefs) =
  { <t.state, t@location, stateDefs[t.state]> | /Transition t := ctl };

rel[str name, loc use, loc def] resolveNames(Controller ctl) =
  relateStates(ctl, collectStates(ctl));