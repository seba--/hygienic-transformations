module lang::missgrant::eval::Step

import lang::missgrant::ast::MissGrant;

alias Output = tuple[str state, list[str] tokens];

public Output eval(Controller ctl, list[str] tokens) {
  senv = stateEnv(ctl);
  eenv = eventEnv(ctl);
  cenv = commandEnv(ctl);
  output = <initial(ctl), []>;
  for (t <- tokens) {
    new = step(t, curr.state, senv, eenv, cenv);
    output = <new.state, output.tokens + new.tokens>;
  }
  return output;
}

public Output step(str token, str state, StateEnv senv, EventEnv eenv, CommandEnv cenv) {
  s1 = senv[state];
  e = eenv[token];
  if (transition(e, s2) <- s1.transitions) {
    return <s2, [ c.token | a <- s2.actions, c <- cenv[a] ]>;
  }
  throw "No transition on <token> in state <state>";
}