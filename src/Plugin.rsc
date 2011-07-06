module Plugin

import lang::missgrant::syntax::MissGrant;
import lang::missgrant::ide::Outline;
import lang::missgrant::utils::Implode;
import lang::missgrant::check::CheckController;
import lang::missgrant::desugar::DesugarResetEvents;
import lang::missgrant::compile::ToSwitch;
import lang::missgrant::compile::ToMethods;
import lang::missgrant::ide::Rename;

import util::IDE;
import ParseTree;
import List;
import IO;

private str CONTROLLER_LANG = "Controller";
private str CONTROLLER_EXT = "ctl";


public void main() {
  registerLanguage(CONTROLLER_LANG, CONTROLLER_EXT, Controller(str input, loc org) {
    return parse(#Controller, input, org);
  });
  
  registerOutliner(CONTROLLER_LANG, node (Controller input) {
    return outlineController(implode(input));
  });
  
  registerAnnotator(CONTROLLER_LANG, Controller (Controller input) {
    msgs = toSet(checkController(implode(input)));
    return input[@messages=msgs];
  });
  
  contribs = {
		popup(
			menu(CONTROLLER_LANG,[
	    		action("Generate Switch", generateSwitch), 
	    		action("Generate Methods", generateMethods),
	    		edit("Rename state", renameState), 
	    		edit("Rename event", renameEvent) 
		    ])
	  	)
  };
	
  registerContributions(CONTROLLER_LANG, contribs);
}

private void generateSwitch(Controller pt, loc l) {
  name = "ControllerSwitch";
  writeFile(|project://missgrant/src/<name>.java|, controller2switch(name, desugarResetEvents(implode(pt))));
}

private void generateMethods(Controller pt, loc l) {
  name = "ControllerMethods";
  writeFile(|project://missgrant/src/<name>.java|, controller2methods(name, desugarResetEvents(implode(pt))));
}

