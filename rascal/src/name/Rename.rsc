module name::Rename

import name::Gensym;
import name::HygienicCorrectness;
import name::Relation;
import name::Names;
import IO;
import Map;
import String;

&T rename(NameGraph G, &T t, ID varId, str new) = renameSubst(G.E, t, (varId:new));

&T rename(Edges refs, &T t, map[ID,str] subst) {
  return visit (t) {
    case str x => setID(subst[getID(x)], getID(x)) 
      when getID(x) in subst
    case str x => setID(subst[def], getID(x)) 
      // XXX: fails to call `refOf`
      // when def := refOf(x@location, refs) && def in subst
      when getID(x) in refs, def := refs[getID(x)], def in subst 
  };
}

&T rename(&T t, map[ID,str] subst) {
  return visit (t) {
    case str x => setID(subst[getID(x)], getID(x)) 
      when getID(x) in subst
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

//&T fixHygiene(NameGraph Gs, &T t, NameGraph(&T) resolveT) 
//  = fixHygiene(Gs, t, renameSubst, resolveT);


&T fixHygiene(type[&T<:node] astType, NameGraph Gs, &T t, NameGraph(&T) resolveT) 
  = x // vvvvv work around Rascal bug.
  when &T x := fixHygiene(Gs, t, rename, resolveT);

@doc {
  Cleaner paper version of fixHygiene that produces exactly the same result.
}
&T fixHygiene(NameGraph Gs, &T t, &T(Edges refs, &T t, map[ID,str] subst) rename, NameGraph(&T) resolveT) {
  <Vs,Es,Ns> = Gs;
  EsClosure = (Es<0,1>)+;
  <Vt,Et,Nt> = resolveT(t);
  
  //println("Source edges: <Es>");
  //println("Target edges: <Et>");
  
  <notPreserveVar1, notPreserveVar2, notPreserveDef> = unhygienicLinks(<Vs,Es,Ns>, <Vt,Et,Nt>);
  allBadBindings = notPreserveVar1 + notPreserveVar2 + notPreserveDef; 
  
  if (allBadBindings == ())
    return t;
  
  Nsrc = ();
  Nsyn = ();
  
  for (vd <- allBadBindings<1>) {
    fresh = gensym(Nt[vd], Nt<1> + Nsrc<1> + Nsyn<1>);
    if (vd in Vs && vd notin Nsrc)
      Nsrc += (vd:fresh) + (v:fresh | v <- Vs, v in EsClosure[vd] || vd in EsClosure[v]);
    else if (vd notin Nsyn) // vd in Vt \ Vs
      Nsyn += (v:fresh | v <- Vt - Vs, nameAt(v, t) == Nt[vd]);
  };
  
  //println("Nsrc: <Nsrc>");
  //println("Nsyn: <Nsyn>");
  
  &T t_new = rename(t, Nsrc + Nsyn);
  
  return fixHygiene(Gs, t_new, rename, resolveT);
}
