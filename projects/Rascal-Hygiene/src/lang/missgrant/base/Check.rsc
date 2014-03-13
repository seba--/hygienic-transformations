module lang::missgrant::base::Check

import lang::missgrant::base::AST;
import Map;
import List;
import Message;
import Relation;
import Set;

/* To check

Errors
- no duplicate event/command decls
- events, commands are declared
- reset events must be in events
- no reset events used in transitions
- statemachine is deterministic
- dead-end states

Warnings
- dead states
- unused action

*/

data Check
  = undefinedStates()
  | undefinedEvents()
  | undefinedCommands()
  | resetsInTransition()
  | duplicateStates()
  | duplicateEvents()
  | duplicateCommands()
  | unreachableStates()
  | nonDeterministicStates()
  | unusedEvents()
  | unusedCommands()
  | deadendStates()
  | noNegativeRetries()
  ;

set[Message] check(list[Check] features, Controller ctl)
  = ( {} | it + check(f, ctl) | f <- features )
  ;

set[Message] check(undefinedStates(), Controller ctl) = undefinedStates(ctl);
set[Message] check(undefinedEvents(), Controller ctl) = undefinedEvents(ctl);
set[Message] check(undefinedCommands(), Controller ctl) = undefinedCommands(ctl);
set[Message] check(resetsInTransition(), Controller ctl) = resetsInTransition(ctl);
set[Message] check(duplicateStates(), Controller ctl) = duplicateStates(ctl);
set[Message] check(duplicateEvents(), Controller ctl) = duplicateEvents(ctl);
set[Message] check(duplicateCommands(), Controller ctl) = duplicateCommands(ctl);
set[Message] check(unreachableStates(), Controller ctl) = unreachableStates(ctl);
set[Message] check(nonDeterministicStates(), Controller ctl) = nonDeterministicStates(ctl);
set[Message] check(unusedEvents(), Controller ctl) = unusedEvents(ctl);
set[Message] check(unusedCommands(), Controller ctl) = unusedCommands(ctl);
set[Message] check(deadendStates(), Controller ctl) = deadendStates(ctl);
set[Message] check(noNegativeRetries(), Controller ctl) = noNegativeRetries(ctl);

public set[Message] check(Controller ctl) 
  = undefinedStates(ctl)
  + undefinedEvents(ctl)
  + undefinedCommands(ctl)
  + resetsInTransition(ctl)
  + duplicateStates(ctl)
  + duplicateEvents(ctl)
  + duplicateCommands(ctl)
  + unreachableStates(ctl)
  + nonDeterministicStates(ctl)
  + unusedEvents(ctl)
  + unusedCommands(ctl)
  + deadendStates(ctl);

public set[Message] undefinedStates(Controller ctl)
  = { error("Undefined state", t@location) | 
           /t:transition(_, q) <- ctl, /state(q, _, _) !:= ctl };

public set[Message] undefinedEvents(Controller ctl)
  = { error("Undefined event", t@location) | 
           /t:transition(e, _) <- ctl, /event(e, _) !:= ctl };

public set[Message] undefinedCommands(Controller ctl) 
  = { error("Undefined command", s@location) | 
           /s:state(_, as, _) <- ctl, a <- as, /command(a, _) !:= ctl };

public set[Message] resetsInTransition(Controller ctl) 
  = { error("Reset used in transition", t@location) | 
        e <- ctl.resets, s <- ctl.states, t:transition(e, _) <- s.transitions }; 

public set[Message] unusedEvents(Controller ctl) 
  = { warning("Unused event", e@location) | e:event(n, _) <- ctl.events, n notin ctl.resets,
           /transition(n, _) !:= ctl }; 

public set[Message] unusedCommands(Controller ctl) 
  = { warning("Unused command", c@location) | c:command(n, _) <- ctl.commands, n notin as }
  when as := ( {} | it + toSet(s.actions) | s <- ctl.states ); 


public set[Message] unreachableStates(Controller ctl) {
  g = { <s.name, t> | s <- ctl.states, /transition(_, t) <- s }*;
  q0 = ctl.states[0].name;
  return { error("Unreachable state", q@location) | q <- ctl.states, q.name notin g[q0] };
}

public set[Message] nonDeterministicStates(Controller ctl) {
   rel[str,str] trans(State q) = { <e, t> | /transition(e, t) <- q };
   bool isDet(rel[str, str] r) = size(domain(r)) == size(r);
   return { error("Non-deterministic state", q@location) | q <- ctl.states, !isDet(trans(q)) };
}

public set[Message] duplicateEvents(Controller ctl) 
  = { error("Duplicate event", l) | l <- locs }
  when locs := duplicates(ctl.events, str(Event e) { return e.name; },
                                   	   loc(Event e) { return e@location; });

public set[Message] duplicateCommands(Controller ctl) 
  = { error("Duplicate command", l) | l <- locs }
  when locs := duplicates(ctl.commands, str(Command c) { return c.name; },
                                   	     loc(Command c) { return c@location; });

public set[Message] duplicateStates(Controller ctl) 
  = { error("Duplicate state", l) | l <- locs }
  when locs := duplicates(ctl.states, str(State s) { return s.name; },
                                   	    loc(State s) { return s@location; });


public set[Message] deadendStates(Controller ctl)
  = { error("Dead-end state", s@location) | /State s <- ctl, s.transitions == [] }
  ;
  
public set[Message] noNegativeRetries(Controller ctl)
  = { error("Retry count must be positive", t@location) | /Transition t <- ctl, t.number < 1 }
  ; 

public set[&V] duplicates(list[&T] lst, &U(&T) fst, &V(&T) snd) {
  tuple[set[&U] xs, set[&V] ys] accu = <{}, {}>;
  accu = ( accu | fst(elt) in it.xs ? it[ys=it.ys + {snd(elt)}] : it[xs=it.xs + {fst(elt)}] | elt <- lst);
  return accu.ys;
}



