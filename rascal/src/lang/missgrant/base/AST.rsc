module lang::missgrant::base::AST

data Controller = controller(list[Event] events, 
                             list[Id] resets, 
                             list[Command] commands,
                             list[State] states);

data State = state(Id name, list[Id] actions, list[Transition] transitions);

data Command = command(Id name, str token);
data Event = event(Id name, str token);
data Transition 
  = transition(Id event, Id state)
  | transition(int number, Id event, Id state)
  ;

data Id = id(str name);

anno loc Controller@location;
anno loc State@location;
anno loc Command@location;
anno loc Event@location;
anno loc Transition@location;
anno loc Id@location;

