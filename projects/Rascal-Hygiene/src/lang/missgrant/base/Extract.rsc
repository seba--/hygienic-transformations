module lang::missgrant::base::Extract

import lang::missgrant::base::AST;

//set[tuple[...]]
alias TransRel = rel[str state, str event,  str toState];
alias ActionRel = rel[str state, str command];

@Require{Desugaring: resetEvents}
TransRel transRel(Controller ctl) 
  = { <s1, t.event, t.state> | /state(s1, _, ts) <- ctl, Transition t <- ts
       /*, event(e, tk) <- ctl.events */};

ActionRel commands(Controller ctl) 
  = { <s, a> | /state(s, as, _) <- ctl, a <- as
      /*, command(a, tk) <- ctl.commands */}; 
