module name::Rename

import name::Gensym;
import name::HygienicCorrectness;
import name::Relation;
import name::Names;
import IO;
import Map;
import String;

&T rename(Edges refs, &T t, map[ID,str] subst) {
  return visit (t) {
    case str x => subst[getID(x)] 
      when getID(x) in subst
    case str x => setID(subst[def], getID(x)) 
      // XXX: fails to call `refOf`
      // when def := refOf(x@location, refs) && def in subst
      when getID(x) in refs, def := refs[getID(x)], def in subst 
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
&T fixHygiene(&S s, &T t, NameGraph(&S) resolveS, NameGraph(&T) resolveT) {
  Gs = <Vs,Es,Ns> = resolveS(s);
  Gt = <Vt,Et,Nt> = resolveT(t);
  
  badDefRefs = ( u:d | <u,d> <- Et<0,1>, u in Vs, u != d, u in Es ? Es[u] != d : true);
  badUseRefs = ( u:d | <u,d> <- Et<0,1>, u notin Vs, d in Vs);
  badNodes = badDefRefs<1> + badUseRefs<1>;
  goodDefRefs = ( u:Es[u] | <u,d> <- badDefRefs<0,1>, u in Es );
  
  //iprintln(badDefRefs);
  //iprintln(badUseRefs);
  //iprintln(goodDefRefs);
  
  if (badNodes == {})
    return t;
  
  usedNames = Nt<1>;
  subst = ();
  
  for (l <- badNodes) {
    fresh = freshName(usedNames, nameOf(l, Gt));
    usedNames += fresh;
    subst += (l : fresh);
  };
  
  Et_new = Et - (badDefRefs + badUseRefs) + goodDefRefs;
  
  t_new = rename(Et_new, t, subst);
  return fixHygiene(s, t_new, resolveS, resolveT);
}
