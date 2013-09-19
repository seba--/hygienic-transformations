module lang::simple::Finishing

import lang::simple::AST;
import lang::simple::Parse;
import lang::simple::Implode;
import lang::simple::Pretty;

import IO;

Prog finishGenProg(Prog p) {
  location = |project://MissGrant/output/debug.sim|;
  text = pretty(p);
  writeFile(location, text);
  return implode(parse(pretty(p), location));
}
