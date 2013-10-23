module lang::missgrant::retries::Desugar

import lang::missgrant::retries::AST;
extend lang::missgrant::base::Desugar;

data Desugaring
  = retries()
  ;

Controller desugar(retries(), Controller ctl) {
  tbl = ( s.name: state2RetryStates(s) | /State s := ctl );
  ctl.states += [ s | k <- tbl, s <- tbl[k] ];
  return visit (ctl) {
    case transition(e, name) => transition(e, tbl[name][-1].name)
      when name in tbl, tbl[name] != [] 
  }
}

list[State] state2RetryStates(State s) 
  = ( [] | it + transition2RetryStates(s, t) | t:transition(e, n, y) <- s.transitions );

list[State] transition2RetryStates(State from, Transition t) {
  list[Transition] normalTransitions(State s) 
    = [ t | t:transition(_, _) <- s.transitions ];

  State makeState(str trg) 
    = state("<from.name>_<t.number>", from.actions, 
         [*normalTransitions(from), transition(t.event, trg)]);
         
  if (t.number == 1) {
    return [makeState(t.state)];
  }
  states = transition2RetryStates(from, t[number = t.number - 1]);
  return [*states, makeState(states[-1].name)];
}

