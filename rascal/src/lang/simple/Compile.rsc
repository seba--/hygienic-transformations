@Requires{Desugaring: resets}
module lang::simple::Compile

import List;

import lang::missgrant::base::AST;
import lang::simple::AST;
import IO;

Prog compile(Controller ctl) = compile(ctl.states);

Prog compile(list[State] states) {
  return prog(
    states2constdefs(states)
    + [state2def(s) | s <- states, bprintln(s)]
    + stateDispatch(states)
    , []);
}

Prog compile(list[State] states, list[str] events) {
  return prog(
    states2constdefs(states)
    + [state2def(s) | s <- states ]
    + stateDispatch(states)
    , [triggerEvents(head(states), events)]);
}


FDef state2constdef(state(str name,_,_), int i) = fdef(name, [], val(nat(i)));

list[FDef] states2constdefs(list[State] states) {
  return [state2constdef(s,i) | <s,i> <- zip(states, [0..size(states)]) ];
}

FDef state2def(state(s, _, trans)) = fdef("<s>-dispatch", ["event"], transitions2condexp(trans, val(error("UnsupportedEvent"))));


// Maybe we will have to parse in the variable symbol "event" to enable hygiene.
Exp transitions2condexp([], Exp deflt) = deflt;


Exp transitions2condexp([t, *ts], Exp deflt) =
  cond( equ(var("event"), val(string(t.event)))
      , call(t.state, [])
      , transitions2condexp(ts, deflt));

FDef stateDispatch(list[State] states) = 
  fdef("main-dispatch",
         ["state", "event"], 
         stateDispatchCondexp(states, val(error("UnsupportedState"))));


Exp stateDispatchCondexp([], Exp deflt) = deflt;

Exp stateDispatchCondexp([s, *ss], Exp deflt) =
  cond( equ(var("state"), call(s.name, []))
       , call("<s.name>-dispatch", [var("event")])
       , stateDispatchCondexp(ss, deflt));

Exp triggerEvents(State init, list[str] es) {
  return 
    ( vardecl("current", call(init.name, [])) 
    | sequ(it, assign("current", call("trans-dispatch", [var("current"), val(string(e))])))
    | e <- es);
}
