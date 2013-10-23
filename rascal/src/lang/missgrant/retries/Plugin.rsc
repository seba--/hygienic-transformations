module lang::missgrant::retries::Plugin

import lang::missgrant::retries::Syntax;
import lang::missgrant::retries::AST;
import lang::missgrant::retries::Parse;
import lang::missgrant::retries::Implode;
import lang::missgrant::retries::Check;
import lang::missgrant::retries::Desugar;
import lang::missgrant::base::Compile;
import lang::missgrant::retries::Outline;

import util::IDE;
import ParseTree;
import Message;
import List;
import String;
import IO;

private str CONTROLLER_LANG = "Controller with Retries";
private str CONTROLLER_EXT = "ctlr";

set[Message] check(Controller ctl)
  = check([undefinedCommands()
  , resetsInTransition()
  , duplicateStates()
  , duplicateEvents()
  , duplicateCommands()
  , unusedCommands()
  , deadendStates()
  , undefinedStates2()
  , undefinedEvents2()
  , unreachableStates2()
  , unusedEvents2()
  , nonDeterministicStates2()], ctl);


Controller desugar(Controller ctl)
  = desugar([Desugaring::resetEvents(), Desugaring::retries()], ctl)
  ;

void main() {
  registerLanguage(CONTROLLER_LANG, CONTROLLER_EXT, Tree(str src, loc l) {
     return parse(src, l);
  });

  contribs = {
             annotator(Tree (Tree pt) {
               return pt[@messages = check(implode(pt))];
             }),

             builder(set[Message] (Tree pt) {
               ctl = desugar(implode(pt));
               out = (pt@\loc)[extension="java"];
               class = split(".", out.file)[0];
               writeFile(out, compile(class, ctl));
               return {};
             })
             ,
             
             outliner(node (Tree input) {
                 return outline(implode(input));
             })
             
  };
  
  registerContributions(CONTROLLER_LANG, contribs);
}
