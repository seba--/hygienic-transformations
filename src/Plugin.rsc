module Plugin

import lang::missgrant::syntax::MissGrant;
import lang::missgrant::ide::Outline;
import lang::missgrant::utils::Implode;

import util::IDE;
import ParseTree;

private str CONTROLLER_LANG = "Controller";
private str CONTROLLER_EXT = "ctl";


public void main() {
  registerLanguage(CONTROLLER_LANG, CONTROLLER_EXT, Controller(str input, loc org) {
    return parse(#Controller, input, org);
  });
  
  registerOutliner(CONTROLLER_LANG, node (&T<:Tree input) {
    if (Controller ctl := input) {
      return outlineController(implode(input));
    }
    throw "Not a controller: <input>";
  });
}