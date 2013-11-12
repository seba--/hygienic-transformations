@Requires{Desugaring: resets}
module lang::simple::Compile

import List;

import lang::missgrant::base::AST;
import lang::simple::AST;

Prog compile(Controller ctl) = compile(ctl.states);

Prog compile(list[State] states) {
  return prog(
    states2constdefs(states)
    + mapper(states, state2def)
    + stateDispatch(states)
    , []);
}

Prog compile(list[State] states, list[str] events) {
  return prog(
    states2constdefs(states)
    + mapper(states, state2def)
    + stateDispatch(states)
    , [triggerEvents(head(states), events)]);
}


FDef state2constdef(State s, int i) {
  return fdef(s.name, [], val(nat(i)));
}

list[FDef] states2constdefs(list[State] states) {
  return [state2constdef(s,i) | <s,i> <- zip(states, [0..size(states)]) ];
}

FDef state2def(State s) {
  return fdef("<s.name>-trans", 
                ["event"], 
                transitions2condexp(s.transitions, val(error("UnsupportedEvent"))));
}

// Maybe we will have to parse in the variable symbol "event" to enable hygiene.
Exp transitions2condexp([], Exp deflt) = deflt;


Exp transitions2condexp([t, *ts], Exp deflt) =
  cond( eq(var("event"), val(string(t.event))) 
      , call(t.state, []) 
      , transitions2condexp(ts, deflt));

FDef stateDispatch(list[State] states) = 
  fdef("trans-dispatch",
         ["state", "event"], 
         stateDispatchCondexp(states, val(error("UnsupportedState"))));


Exp stateDispatchCondexp([], Exp deflt) = deflt;

Exp stateDispatchCondexp([s, *ss], Exp deflt) =
  cond( eq(var("state"), call(s.name, []))
       , call("<s.name>-trans", [var("event")])
       , stateDispatchCondexp(ss, deflt));

Exp triggerEvents(State init, list[str] es) {
  return 
    ( vardecl("current", call(init.name, [])) 
    | seq(it, assign("current", call("trans-dispatch", [var("current"), val(string(e))])))
    | e <- es);
}
