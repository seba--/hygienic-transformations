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


&T rename(set[Reference] refs, &T t, loc varLoc, &U new) {
  return visit (t) {
    case &U x => new[@location = x@location] 
      when x@location == varLoc
    case &U x => new[@location = x@location] 
      when varLoc == ref(x@location, G)
  };
}

&T rename(set[Reference] refs, &T t, map[loc,&U] subst) {
  return visit (t) {
    case &U x => subst[x@location][@location = x@location] 
      when x@location in subst
    case &U x => subst[def][@location = x@location] 
      // XXX: fails to call `refOf`
      // when def := refOf(x@location, refs) && def in subst
      when {def} := refs[x@location] && def in subst 
  };
}


&T fixHygiene(NameGraph Gs, NameGraph Gt, &T t, &U(str) name2var) {
  set[Reference] badRefs = sourceNotPreserved(Gs, Gt) + synthesizedCaptured(Gs, Gt);
  synth = synthesizedLabels(Gs, Gt);
  set[loc] renameLocs 
    = ({} | it + (l1 in synth ? {l1} : {}) + (l2 in synth ? {l2} : {}) 
          | <l1,l2> <- badRefs );
  
  renameNames = {<l,nameOf(l, Gt)> | l <- renameLocs};
  
  usedNames = names(Gt);
  newNames = {};
  map[loc, &U] subst = ();
  for (<l,n> <- renameNames) {
    str fresh = freshName(usedNames, n);
	usedNames += fresh;
	freshVar = name2var(fresh);
	subst += (l:freshVar);
	newNames += <fresh,l>;
  };
  
  renamed = rename(Gt[1] - badRefs, t, subst);
  return renamed;
}

@doc {
  Cleaner paper version of fixHygiene that produces exactly the same result.
}
&T fixHygiene_clean(&S s, &T t, NameGraph(&S) resolveS, NameGraph(&T) resolveT, &U(str) name2var) {
  Gs = <Vs,Es,Ns> = resolveS(s);
  Gt = <Vt,Et,Nt> = resolveT(t);
  
  badDefRefs = { <u,d> | <u,d> <- Et, u in Vs, u != d, <u,d> notin Es};
  badUseRefs = { <u,d> | <u,d> <- Et, u notin Vs, d in Vs};
  badNodes = badDefRefs<1> + badUseRefs<0>;
  
  if (badNodes == {})
    return t;
  
  usedNames = Nt<1>;
  subst = ();
  
  for (l <- badNodes) {
    fresh = freshName(usedNames, nameOf(l, Gt));
    usedNames += fresh;
	freshVar = name2var(fresh);
    subst += (l : freshVar);
  };
  
  Et_new = Et - (badDefRefs + badUseRefs);
  
  Prog t_new = rename(Et_new, t, subst);
  return fixHygiene_clean(s, t_new, resolveS, resolveT, name2var);
}
