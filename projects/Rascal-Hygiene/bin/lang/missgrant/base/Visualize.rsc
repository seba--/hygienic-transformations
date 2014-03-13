module lang::missgrant::base::Visualize

import lang::missgrant::base::AST;
import lang::missgrant::base::Extract;

import vis::Figure;
import List;
import util::Math;
import IO;
import util::Resources;
import vis::Render;
import Relation;
import Set;

void renderController(Controller ctl) {
  stateMachineVisInterface(transRel(ctl), commands(ctl), ctl.states[0].name);
}


Figure stateMachineGraph(TransRel trans, str init, str state) {
	  str getColor(str s) = (s == state) ? "red" : ((s == init) ? "green" : "lightskyblue");

	  list[Figure] nodes = [ box(text(i), left(), top(), fillColor(getColor(i)), grow(1.2), id(i))  
	                         | i <-[init] + toList(domain(trans)) - init ] 
	       + [ ellipse(text(labelS), grow(1.1), left(), top(), id("<fromS>,<labelS>,<toS>"))  
	           | <fromS,labelS,toS> <- trans ];

	  Edges edges = [ edge(fromS, "<fromS>,<labelS>,<toS>", triangle(10, fillColor("black"))),
						                  edge("<fromS>,<labelS>,<toS>",toS,triangle(10,fillColor("black")))
				                  | <fromS,labelS,toS> <-trans];
	
	  return graph(nodes, edges, hint("layered"), width(900), height(1000), top(), gap(70));
	}

void stateMachineVisInterface(TransRel trans, ActionRel commands, str init){
	  str cur = init;
	  list[str] eventsTokens = [];
	  list[str] commandsTokens = [];
	
	  void handleToken(str token) {
		    if(c <- trans[cur,token]) {
			      cur = c;
			      commandsTokens+=toList(commands[cur]);
		    }
  	  }
	
	  void () getAddInputHandler(str tokenName) {
		     return void () { eventsTokens+=[tokenName]; handleToken(tokenName); };
	  }
	  return render(computeFigure(Figure() { return vcat([
		     hcat([button(ev, getAddInputHandler(ev)) | ev <- trans<1>], vresizable(false)),
		     hcat([text("Events:")] + [box(text(ev), grow(1.1)) | ev <-eventsTokens], resizable(false)),
		     hcat([text("Commands:")] + [box(text(comm), grow(1.1)) | comm <-commandsTokens], resizable(false)),
		     stateMachineGraph(trans,init,cur)]); }));
}
		
