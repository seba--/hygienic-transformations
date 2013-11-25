module name::NameFix

import name::Gensym;
import name::HygienicCorrectness;
import name::Relation;
import name::Names;
import IO;
import Map;
import String;


&T rename(&T t, map[ID,str] subst) {
  return visit (t) {
    case str x => setID(subst[getID(x)], getID(x)) 
      when getID(x) in subst
  };
}


Edges badBindings(<Vs,Es,Ns>, <Vt,Et,Nt>) {
  notPreserveVar1 =    (v:Et[v] | v <- Et<0>, v in Vs, v in Es, Es[v] != Et[v]);
  notPreserveVar2 =    (v:Et[v] | v <- Et<0>, v in Vs, v notin Es, v != Et[v]);
  notPreserveDef  =    (v:Et[v] | v <- Et<0>, v notin Vs, Et[v] in Vs);
  
  //println("not preserve source vars 1: <notPreserveVar1>");
  //println("not preserve source vars 2: <notPreserveVar2>");
  //println("not preserve source defs  : <notPreserveDef>");
  
  return notPreserveVar1 + notPreserveVar2 + notPreserveDef;
}

tuple[map[ID,str],map[ID,str]] compRenamings(<Vs,Es,Ns>, <Vt,Et,Nt>, t, badDefs) {
  Nsrc = ();
  Nsyn = ();
  
  for (vd <- badDefs) {
    fresh = gensym(Nt[vd], Nt<1> + Nsrc<1> + Nsyn<1>);
    if (vd in Vs && vd notin Nsrc)
      Nsrc += (vd:fresh) + (v:fresh | v <- Es<0>, Es[v] == vd);
    else if (vd notin Nsyn) // vd in Vt \ Vs
      Nsyn += (v:fresh | v <- Vt - Vs, nameAt(v, t) == Nt[vd]);
  };
  
  //println("Nsrc: <Nsrc>");
  //println("Nsyn: <Nsyn>");
  
  return <Nsrc,Nsyn>;
}

&T nameFix(type[&T<:node] astType, NameGraph Gs, &T t, NameGraph(&T) resolveT) 
  = x // vvvvv work around Rascal bug.
  when &T x := nameFix(Gs, t, rename, resolveT);

&T nameFix(NameGraph Gs, &T t, &T(&T t, map[ID,str] subst) rename, NameGraph(&T) resolveT) {
  Gt = resolveT(t);
  
  //println("Source edges: <Gs.E>");
  //println("Target edges: <Gt.E>");
  
  allBadBindings = badBindings(Gs, Gt); 
  if (allBadBindings == ()) return t;
  
  <Nsrc,Nsyn> = compRenamings(Gs, Gt, t, allBadBindings<1>);
  &T t_new = rename(t, Nsrc + Nsyn);
  
  return nameFix(Gs, t_new, rename, resolveT);
}
