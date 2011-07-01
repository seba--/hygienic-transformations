module lang::missgrant::check::CheckController

import lang::missgrant::ast::MissGrant;
import Map;
import Message;

/* To check

- no duplicate event/command decls
- events, commands are declared
- reset events must be in events
- no reset events used in transitions
- statemachine is deterministic

Warnings
- dead states
- unused action

*/

public list[Message] checkController(Controller ctl) {
  env = ();
  errors = for (e:event(n, t) <- ctl.events) {
     if (n in env) { 
       append error("Duplicate event", e@location);
     }
     else {
       if (t in invert(env)) 
         append error("Duplicate event token", e@location);
       env[n] = t;
     }
  } 
  
  env = ();
  errors += for (c:command(n, t) <- ctl.commands) {
     if (n in env) 
       append error("Duplicate command", e@location);
     if (t in invertUnique(env)) 
       append error("Duplicate command token", e@location);
      env[n] = t;
  }
  
  errors += for (e <- ctl.resets, s <- ctl.states, t:transition(e, _) <- s.transitions) 
     append error("Reset event used in transition", t@location);
  
  errors += for (s <- ctl.states) {
    seen = {};
    for (t:transition(e, _) <- s.transitions) {
      if (e in seen) 
        append error("Non-determinism", t@location);
      seen += {e};
    }
  }
  
  cmds = [ n | command(n, _) <- ctl.commands ];
  evs = [ n | event(n, _) <- ctl.events ];
  sts = [ n | state(n, _, _) <- ctl.states ];
  
  errors += for (e <- ctl.resets, e notin evs) 
    append error("Undeclared reset event", ctl@location);
  
  errors += for (s <- ctl.states) {
  	for (a <- s.actions, a notin cmds)
      append error("Undeclared action used", s@location);
    for (t:transition(e, _) <- s.transitions, e notin evs)  
      append error("Undeclared event", t@location);
    for (t:transition(_, s2) <- s.transitions, s2 notin sts)  
      append error("Undeclared state", s2@location);
  }
  
  return errors;
}
