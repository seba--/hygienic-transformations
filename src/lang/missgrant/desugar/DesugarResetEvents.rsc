module lang::missgrant::desugar::DesugarResetEvents

import  lang::missgrant::ast::MissGrant;

public Controller desugarResetEvents(Controller ctl) {
  return visit (ctl) {
    case state(n, as, ts) => state(n, as, 
    		ts + [ transition(e, "idle") | e <- ctl.resets ])
  }
}