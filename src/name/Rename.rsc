module name::Rename

import name::Relation;
import IO;


// BUG: this import should not be necessary, 
//      but without name::Test::renameS1() yields
//      "Undeclared annotation: location on State"
import lang::missgrant::base::AST;
import lang::simple::AST;


&T rename(NameRel r, &T t, loc varLoc, &U new) {
  m = (k:v | <k,v> <- r<1,2>);
  
  println(m);
  println(varLoc);
  
  if (varLoc in m)
    varLoc = m[varLoc];

  

  return visit (t) {
    case &U x => new[@location = x@location] 
      when x@location == varLoc
    case &U x => new[@location = x@location] 
      when x@location in m && m[x@location] == varLoc
  };
}