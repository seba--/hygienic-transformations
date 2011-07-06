module lang::missgrant::vis::ShowStateMachine

import lang::missgrant::ast::MissGrant;
import vis::Figure;
import List;
import Real;
import Integer;

public FProperty popup(str S,FProperty props...){
   return mouseOver(box(text(S), [fillColor("lightyellow"),grow(1.2),resizable(false),mouseStick(false)] + props));
}

public Figure triangle(int side,FProperty props...){
  return overlay([point(left(),bottom()),point(top()), point(right(),bottom())], 
  	[shapeConnected(true), shapeClosed(true),  size(side,sqrt(3.0/4.0) * toReal(side)),
  	resizable(false)] + props);
}

public str actionList(list[str] actions,CommandEnv commandEnvironment){
	str actionStr(str action){
		return "<action>(<commandEnvironment[action].token>)";
	}

	if(actions == []) return "\<No actions\>";
	return (actionStr(head(actions)) | it + ", <actionStr(action)>)"  | action <- tail(actions));
} 

public Figure stateMachineGraph(Controller c){
	commandEnvironment = commandEnv(c);
	list[Figure] nodes = [box(text(c.states[i].name)
							,fillColor((i == 0) ? "green" : "lightskyblue"),grow(2.5),id(c.states[i].name)
							,popup(actionList(c.states[i].actions,commandEnvironment))) 
							| i <- [0..size(c.states)-1]];
	Edges edges = [[ edge(
						st.name,
						trans.state,
						label(ellipse(text(trans.event),grow(1.5))),
						toArrow(triangle(10,fillColor("black")))
					)
				 | trans <- st.transitions]   | st <- c.states];
	return graph(nodes,edges,hint("layered"),width(800),height(800),gap(120));
}