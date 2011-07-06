module lang::missgrant::eval::Step

import lang::missgrant::ast::MissGrant;
import IO;

alias Output = tuple[ControllerState, list[str]];

public Output eval(Controller ctl, list[str] tokens) {
  state = initialControllerState(ctl);
  actionsFired = [];
  for (t <- tokens) {
    <state,newActions> = step(state,t);
    actionsFired += newActions;
  }
  return <state,actionsFired>;
}

public Output step(ControllerState state, str eventToken) {
  eventName = state.eventTokenToName[eventToken];
  curState = state.stateEnv[state.curStateName];
  actionsFired = [];
  str newStateName = "";
  bool moved = false;
  
  if(eventName <- state.ctl.resets){
  	newStateName = initial(state.ctl).name;
  	moved = true;
  } else if(transition(eventName,ns) <- curState.transitions ) {
  	newStateName = ns;
  	moved = true;
  }
 
  if(moved){
  	state.curStateName = newStateName;
  	return <state, [ state.commandNameToToken[n] | n <- state.stateEnv[newStateName].actions]>;
  } else {
  	return <state,[]>;
  }
}