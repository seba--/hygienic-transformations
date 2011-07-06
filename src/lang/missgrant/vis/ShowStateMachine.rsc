module lang::missgrant::vis::ShowStateMachine

import lang::missgrant::ast::MissGrant;
import lang::missgrant::eval::Step;
import vis::Figure;
import List;
import Real;
import Integer;
import IO;

public FProperty popup(str S,FProperty props...){
   return mouseOver(box(text(S), [fillColor("lightyellow"),grow(1.2),resizable(false),mouseStick(false)] + props));
}

public Figure triangle(int side,FProperty props...){
  return overlay([point(left(),bottom()),point(top()), point(right(),bottom())], 
  	[shapeConnected(true), shapeClosed(true),  size(side,sqrt(3.0/4.0) * toReal(side)),
  	resizable(false)] + props);
}

public str actionList(list[str] actions,map[str,str] commandNameToToken){
	str actionStr(str action){
		return "<action>(<commandNameToToken[action]>)";
	}

	if(actions == []) return "\<No actions\>";
	return (actionStr(head(actions)) | it + ", <actionStr(action)>)"  | action <- tail(actions));
} 

public Figure stateMachineGraph(ControllerState s){
	str getColor(int i){
		return (s.ctl.states[i].name == s.curStateName) ? "red" : ((i == 0) ? "green" : "lightskyblue");
	} 
	list[Figure] nodes = [box(text(s.ctl.states[i].name)
							,fillColor(getColor(i)),grow(2.5),id(s.ctl.states[i].name)
							,popup(actionList(s.ctl.states[i].actions,s.commandNameToToken))) 
							| i <- [0..size(s.ctl.states)-1]];
	Edges edges = [[ edge(
						st.name,
						trans.state,
						label(ellipse(text(trans.event),grow(1.5))),
						toArrow(triangle(10,fillColor("black")))
					)
				 | trans <- st.transitions]   | st <- s.ctl.states];
	return graph(nodes,edges,hint("layered"),width(800),height(300),gap(120));
}

public Figure stateMachineVis(Controller c){
	ControllerState state = initialControllerState(c);
	
	list[str] eventsTokens = [];
	list[str] commandsTokens = [];
	
	void handleToken(str token){
		println("Before : <state.curStateName> ");
		<state, newCmds> = step(state,token);
		println("After : <state.curStateName> ");
		commandsTokens+=newCmds;
		
	}
	
	void () getAddInputHandler(str tokenName){
		return void () { eventsTokens+=[tokenName]; handleToken(tokenName); };
	}
	
	return computeFigure(Figure() { return vcat([
		hcat([button(ev.name,getAddInputHandler(ev.token)) | ev <- c.events]),
		hcat([text("Events:")] + [box(text(ev),popup(state.eventTokenToName[ev]),grow(1.3)) | ev <-eventsTokens],resizable(false)),
		hcat([text("Commands:")] + [box(text(comm),popup(state.commandTokenToName[comm]),grow(1.3)) | comm <-commandsTokens],resizable(false)),
		stateMachineGraph(state)]); });
}
		