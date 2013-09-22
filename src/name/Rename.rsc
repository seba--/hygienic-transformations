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
  
  if (varLoc in m)
    varLoc = m[varLoc];

  return visit (t) {
    case &U x => new[@location = x@location] 
      when x@location == varLoc
    case &U x => new[@location = x@location] 
      when x@location in m && m[x@location] == varLoc
  };
}

&T rename(NameRel r, &T t, map[loc,&U] subst) {
  m = (k:v | <k,v> <- r<1,2>);
  
  subst = ((k in m ? m[k] : k):v | <k,v> <- subst);

  return visit (t) {
    case &U x => subst[x@location][@location = x@location] 
      when x@location in subst
    case &U x => subst[m[x@location]][@location = x@location] 
      when x@location in m && m[x@location] in subst
  };
}


NameRel fixHygiene(NameRel sNames, NameRel tNames) {
  badLinks = sourcePreservation(sNames, tNames) + synthesizedNotCaptured(sNames, tNames);
  synth = 0;
  rename = ({} | it + (l1 in synth ? {l1} : {}) + (l2 in synth ? {l2} : {}) 
               | <l1,l2> <- badLinks );
}