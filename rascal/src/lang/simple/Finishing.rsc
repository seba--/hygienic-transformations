module lang::simple::Finishing

import lang::simple::AST;
import lang::simple::Parse;
import lang::simple::Implode;
import lang::simple::Pretty;

import IO;
import util::Maybe;
import List;

loc writeDebugFile(Prog p) {
  location = |project://Rascal-Hygiene/output/debug.sim|;
  text = pretty(p);
  writeFile(location, text);
  return location;
}

Prog finishGenProg(Prog p) {
  location = writeDebugFile(p);
  q = implode(parse(pretty(p), location));
  
  lst = [];
  
  visit (p) {
    case Var v: 
        lst += [(v@location?) ? just(v@location) : nothing()];
  }
  
  return visit (q) {
    case Var v: {
	  <mx, lst> = pop(lst);
	  if (just(x) := mx) 
	    insert v[@location=x];
	}
  }
}
