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
  return define(var(s.name)[@location = s@location], [], val(nat(i)));
}

list[Def] states2constdefs(list[State] states) {
  return [state2constdef(s,i) | <s,i> <- zip(states, [0..size(states)]) ];
}

Def state2def(State s) {
  return define(var("<s.name>-trans"), 
                [var("event")], 
                transitions2condexp(s.transitions, val(error("UnsupportedEvent"))));
}

// Maybe we will have to parse in the variable symbol "event" to enable hygiene.
Exp transitions2condexp([], Exp deflt) = deflt;

Exp mkvar(str name) = evar(var(name));
Exp mkvar(str name, loc l) = evar(var(name)[@location = l]);

Exp transitions2condexp([t, *ts], Exp deflt) = 
  cond( eq(mkvar("event"), val(string(t.event))) 
      , call(var(t.state)[@location = t@location], []) 
      , transitions2condexp(ts, deflt));

Def stateDispatch(list[State] states) = 
  define(var("trans-dispatch"),
         [var("state"), var("event")], 
         stateDispatchCondexp(states, val(error("UnsupportedState"))));


Exp stateDispatchCondexp([], Exp deflt) = deflt;

Exp stateDispatchCondexp([s, *ss], Exp deflt) =
  cond( eq(mkvar("state"), call(var(s.name)[@location = s@location], []))
       , call(var("<s.name>-trans"), [mkvar("event")])
       , stateDispatchCondexp(ss, deflt));

Exp triggerEvents(State init, list[str] es) {
  return 
    ( assign(var("current"), call(var(init.name)[@location = init@location], [])) 
    | seq(it, assign(var("current"), call(var("trans-dispatch"), [mkvar("current"), val(string(e))])))
    | e <- es);
}