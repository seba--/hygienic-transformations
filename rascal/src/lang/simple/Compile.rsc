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


Def state2constdef(State s, int i) {
  return define(s.name, [], val(nat(i)));
}

list[Def] states2constdefs(list[State] states) {
  return [state2constdef(s,i) | <s,i> <- zip(states, [0..size(states)]) ];
}

Def state2def(State s) {
  return define("<s.name>-trans", 
                ["event"], 
                transitions2condexp(s.transitions, val(error("UnsupportedEvent"))));
}

// Maybe we will have to parse in the variable symbol "event" to enable hygiene.
Exp transitions2condexp([], Exp deflt) = deflt;

Exp mkvar(str name) = evar(name);
Exp mkvar(str name, loc l) = evar(name);

Exp transitions2condexp([t, *ts], Exp deflt) = 
  cond( eq(mkvar("event"), val(string(t.event))) 
      , call(t.state, []) 
      , transitions2condexp(ts, deflt));

Def stateDispatch(list[State] states) = 
  define("trans-dispatch",
         ["state", "event"], 
         stateDispatchCondexp(states, val(error("UnsupportedState"))));


Exp stateDispatchCondexp([], Exp deflt) = deflt;

Exp stateDispatchCondexp([s, *ss], Exp deflt) =
  cond( eq(mkvar("state"), call(s.name, []))
       , call("<s.name>-trans", [mkvar("event")])
       , stateDispatchCondexp(ss, deflt));

Exp triggerEvents(State init, list[str] es) {
  return 
    ( assign("current", call(init.name, [])) 
    | seq(it, assign("current", call("trans-dispatch", [mkvar("current"), val(string(e))])))
    | e <- es);
}