module lang::missgrant::base::AST

data Controller = controller(list[Event] events, 
                             list[str] resets, 
                             list[Command] commands,
                             list[State] states);

data State = state(str name, list[str] actions, list[Transition] transitions);

data Command = command(str name, str token);
data Event = event(str name, str token);
data Transition 
  = transition(str event, str state)
  | transition(str event, str state, int number, str then)
  ;


anno loc Controller@location;
anno loc State@location;
anno loc Command@location;
anno loc Event@location;
anno loc Transition@location;

