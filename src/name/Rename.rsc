module name::Rename

import name::Gensym;
import name::HygienicCorrectness;
import name::Relation;
import IO;


// BUG: this import should not be necessary, 
//      but without name::Test::renameS1() yields
//      "Undeclared annotation: location on State"
import lang::missgrant::base::AST;
import lang::simple::AST;


&T rename(NameGraph r, &T t, loc varLoc, &U new) {
  <V,E> = r;
  
  //if (varLoc in m)
  //  varLoc = m[varLoc];

  return visit (t) {
    case &U x => new[@location = x@location] 
      when x@location == varLoc
    case &U x => new[@location = x@location] 
      when <x@location, varLoc> in E
  };
}

&T rename(NameGraph r, &T t, map[loc,&U] subst) {
  <V,E> = r;
  
  //subst = ((k in m ? m[k] : k):v | <k,v> <- subst<0,1>);

  return visit (t) {
    case &U x => subst[x@location][@location = x@location] 
      when x@location in subst
    case &U x => v0[@location = x@location] 
      when {v0} := {v | <d,v> <- subst<0,1>, <x@location,d> in E}
  };
}


&T fixHygiene(NameGraph sNames, NameGraph tNames, &T t, &U(str) name2var) {
  set[Link] badLinks = sourcePreservation(sNames, tNames) + synthesizedNotCaptured(sNames, tNames);
  synth = synthesizedLabels(sNames, tNames);
  set[loc] renameLocs 
    = ({} | it + (l1 in synth ? {l1} : {}) + (l2 in synth ? {l2} : {}) 
          | <l1,l2> <- badLinks );
  
  rel[loc,str] tNameMap = tNames[0]<1,0>;
  renameNames = {<n,l> | l <- renameLocs, {n} := tNameMap[l]};
  
  usedNames = tNames<0><0>;
  newNames = {};
  map[loc, &U] subst = ();
  for (<n,l> <- renameNames) {
    str fresh = freshName(usedNames, n);
	usedNames += fresh;
	freshVar = name2var(fresh);
	subst += (l:freshVar);
	newNames += <fresh,l>;
  };
  
  fixedTNames = <tNames[0] - renameNames + newNames,tNames[1] - badLinks>;
  //println(fixedTNames);
  renamed = rename(fixedTNames, t, subst);
  return renamed;
}

