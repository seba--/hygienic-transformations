module Plugin

import lang::missgrant::syntax::MissGrant;
import lang::missgrant::ast::MissGrant;
import lang::missgrant::ide::Outline;
import lang::missgrant::utils::Implode;
import lang::missgrant::check::CheckController;
import lang::missgrant::desugar::DesugarResetEvents;
import lang::missgrant::compile::ToSwitch;
import lang::missgrant::compile::ToMethods;
import lang::missgrant::ide::Rename;
import lang::missgrant::vis::ShowStateMachine;
import lang::missgrant::extract::ToRelation;

import util::IDE;
import vis::Render;
import ParseTree;
import List;
import IO;

private str CONTROLLER_LANG = "Controller";
private str CONTROLLER_EXT = "ctl";


public void main() {
  registerLanguage(CONTROLLER_LANG, CONTROLLER_EXT, lang::missgrant::syntax::MissGrant::Controller(str input, loc org) {
    return parse(#lang::missgrant::syntax::MissGrant::Controller, input, org);
  });
  
  registerOutliner(CONTROLLER_LANG, node (lang::missgrant::syntax::MissGrant::Controller input) {
    return outlineController(implode(input));
  });
  
  registerAnnotator(CONTROLLER_LANG, lang::missgrant::syntax::MissGrant::Controller (lang::missgrant::syntax::MissGrant::Controller input) {
    msgs = toSet(checkController(implode(input)));
    return input[@messages=msgs];
  });
  
  contribs = {
		popup(
			menu(CONTROLLER_LANG,[
	    		action("Generate Switch", generateSwitch), 
	    		action("Generate Methods", generateMethods),
	    		action("Visualize", visualizeController),
	    		edit("Rename...", rename) 
		    ])
	  	)
  };
	
  registerContributions(CONTROLLER_LANG, contribs);
}

private void generateSwitch(lang::missgrant::syntax::MissGrant::Controller pt, loc l) {
  name = "ControllerSwitch";
  writeFile(|project://missgrant/src/<name>.java|, controller2switch(name, desugarResetEvents(implode(pt))));
}

private void generateMethods(lang::missgrant::syntax::MissGrant::Controller pt, loc l) {
  name = "ControllerMethods";
  writeFile(|project://missgrant/src/<name>.java|, controller2methods(name, desugarResetEvents(implode(pt))));
}

private void visualizeController(lang::missgrant::syntax::MissGrant::Controller pt, loc l) {
  ast = implode(pt);
  render(stateMachineVisInterface(transRel(ast), commands(ast), ast.states[0].name));
}
