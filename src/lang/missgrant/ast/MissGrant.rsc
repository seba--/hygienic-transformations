module lang::missgrant::ast::MissGrant

data Controller = controller(list[Event] events, 
					list[str] resets, 
					list[Command] commands,
					list[State] states);

data State = state(str name, list[str] actions, list[Transition] transitions);

data Command = command(str name, str token);
data Event = event(str name, str token);
data Transition = transition(str event, str state);

