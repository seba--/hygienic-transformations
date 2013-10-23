module lang::simple::Plugin

import lang::simple::Syntax;

import util::IDE;
import util::Prompt;
import vis::Render;
import Message;
import ParseTree;
import List;
import String;
import IO;

private str CONTROLLER_LANG = "Simple";
private str CONTROLLER_EXT = "sim";

void main() {
  registerLanguage(CONTROLLER_LANG, CONTROLLER_EXT, Tree(str src, loc l) {
     return parse(#start[Prog], src, l);
  });
}
