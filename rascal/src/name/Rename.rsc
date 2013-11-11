module name::Rename

import name::Gensym;
import name::HygienicCorrectness;
import name::Relation;
import IO;
import Map;


// BUG: this import should not be necessary, 
//      but without name::Test::renameS1() yields
//      "Undeclared annotation: location on State"
import lang::missgrant::base::AST;
import lang::simple::AST;


&T rename(Edges refs, &T t, loc varLoc, &U new) {
  return visit (t) {
    case &U x => new[@location = x@location] 
      when x@location == varLoc
    case &U x => new[@location = x@location] 
      when varLoc == refOf(x@location, G)
  };
}

&T rename(Edges refs, &T t, map[loc,&U] subst) {
  return visit (t) {
    case &U x => subst[x@location][@location = x@location] 
      when x@location in subst
    case &U x => subst[def][@location = x@location] 
      // XXX: fails to call `refOf`
      // when def := refOf(x@location, refs) && def in subst
      when x@location in refs && def := refs[x@location] && def in subst 
  };
}


//&T fixHygiene(NameGraph Gs, NameGraph Gt, &T t, &U(str) name2var) {
//  Edges badRefs = sourceNotPreserved(Gs, Gt) + synthesizedCaptured(Gs, Gt);
//  synth = synthesizedNodes(Gs, Gt);
//  set[loc] renameLocs 
//    = ({} | it + (l1 in synth ? {l1} : {}) + (l2 in synth ? {l2} : {}) 
//          | <l1,l2> <- badRefs<0,1> );
//  
//  usedNames = namesOf(Gt);
//  map[loc, &U] subst = ();
//  for (l <- renameLocs) {
//    str fresh = freshName(usedNames, Gt.N[l]);
//	usedNames += fresh;
//	freshVar = name2var(fresh);
//	subst += (l:freshVar);
//  };
//  
//  renamed = rename(Gt[1] - badRefs, t, subst);
//  return renamed;
//}

@doc {
  Cleaner paper version of fixHygiene that produces exactly the same result.
}
Prog fixHygiene(&T t, <Vs,Es,Ns>, NameGraph(&T) resolveT, &U(str) name2var) {
  Gt = <Vt,Et,Nt> = resolveT(t);
  
  //iprintln(Es);
  //iprintln(Et);
  
  badDefRefs  = (u:Et[u] | u <- Vs & Vt, u in Es, u in Et, Es[u] != Et[u]);
  badUseRefs  = (u:d     | d <- Vs & Vt, u <- Es & Et, Et[u] == d, Es[u] != d);
  badSelfRefs = (u:Et[u] | u <- Vs & Vt, u in Et, u notin Es, u != Et[u]);

  badDefinitionNodes = badDefRefs<1> + badUseRefs<1> + badSelfRefs<1>;
  
  goodDefRefs = ( u:Es[u] | u <- badDefRefs<0>);
  // goodUseRefs required?
  goodUseRefs = ( u:d | d <- badUseRefs<1>, u <- Es, Es[u] == d);
  
  iprintln(badDefRefs);
  iprintln(badUseRefs);
  //iprintln(badSelfRefs);
  //iprintln(goodDefRefs);
  
  if (badDefinitionNodes == {})
    return t;
  
  usedNames = Nt<1>;
  subst = ();
  
  for (l <- badDefinitionNodes) {
    fresh = freshName(usedNames, nameOf(l, Gt));
    usedNames += fresh;
    freshVar = name2var(fresh);
    subst += (l : freshVar);
  };
  
  Et_new = Et - (badDefRefs + badUseRefs + badSelfRefs) + goodDefRefs + goodUseRefs;
  
  Prog t_new = rename(Et_new, t, subst);
  return fixHygiene(t_new, <Vs,Es,Ns>, resolveT, name2var);
}
