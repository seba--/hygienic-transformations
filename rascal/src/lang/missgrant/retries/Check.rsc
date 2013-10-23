module lang::missgrant::retries::Check

import lang::missgrant::retries::AST;
extend lang::missgrant::base::Check;
import Message;
import IO;

data Check
  = undefinedStates2()
  | undefinedEvents2()
  | unusedEvents2()
  | unreachableStates2()
  | nonDeterministicStates2()
  ;

set[Message] check(undefinedStates2(), Controller ctl) = undefinedStates2(ctl);
set[Message] check(undefinedEvents2(), Controller ctl) = undefinedEvents2(ctl);
set[Message] check(unreachableStates2(), Controller ctl) = unreachableStates2(ctl);
set[Message] check(nonDeterministicStates2(), Controller ctl) = nonDeterministicStates2(ctl);
set[Message] check(unusedEvents2(), Controller ctl) = unusedEvents2(ctl);


set[Message] undefinedStates2(Controller ctl)
  = undefinedStates(ctl) + 
     { error("Undefined state", t@location) | 
           /t:transition(_,  _, q) <- ctl, /state(q, _, _) !:= ctl };

set[Message] undefinedEvents2(Controller ctl)
  = undefinedStates(ctl) +
     { error("Undefined event", t@location) | 
           /t:transition(e, _,  _) <- ctl, /event(e, _) !:= ctl };

set[Message] resetsInTransition2(Controller ctl) 
  = resetsInTransition(ctl) +
     { error("Reset used in transition", t@location) | 
        e <- ctl.resets, s <- ctl.states, t:transition(e, _, _) <- s.transitions }; 

set[Message] unusedEvents2(Controller ctl) 
  = { warning("Unused event", e@location) | e:event(n, _) <- ctl.events, n notin ctl.resets,
           (/transition(n, _) !:= ctl && /transition(n, _, _) !:= ctl) }; 


set[Message] unreachableStates2(Controller ctl) {
  g = { <s.name, t> | s <- ctl.states, /transition(_, t) <- s }
     + { <s.name, t> | s <- ctl.states, /transition(_, _, t) <- s };
  g = g*;
  q0 = ctl.states[0].name;
  return { error("Unreachable state", q@location) | q <- ctl.states, q.name notin g[q0] };
}

set[Message] nonDeterministicStates2(Controller ctl) {
   rel[str,str] trans(State q) = { <e, t> | /transition(e, t) <- q }
     + { <e, t> | /transition(e, _, t) <- q };
   bool isDet(rel[str, str] r) = size(domain(r)) == size(r);
   return { error("Non-deterministic state", q@location) | q <- ctl.states, !isDet(trans(q)) };
}

