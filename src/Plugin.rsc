module Plugin

import lang::missgrant::syntax::MissGrant;
import lang::missgrant::ide::Outline;
import lang::missgrant::utils::Implode;
import lang::missgrant::check::CheckController;

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
    iprint(msgs);
    return input[@messages=msgs];
  });
}