module name::NameFix

import name::Gensym;
import name::HygienicCorrectness;
import name::Relation;
import name::Figs;
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


Edges badBindings(<Vs,Es>, <Vt,Et>) {
  notPreserveVar1 =    (v:Et[v] | v <- Et<0>, v in Vs, v in Es, Es[v] != Et[v]);
  notPreserveVar2 =    (v:Et[v] | v <- Et<0>, v in Vs, v notin Es, v != Et[v]);
  notPreserveDef  =    (v:Et[v] | v <- Et<0>, v notin Vs, Et[v] in Vs);
  
  //println("not preserve source vars 1: <notPreserveVar1>");
  //println("not preserve source vars 2: <notPreserveVar2>");
  //println("not preserve source defs  : <notPreserveDef>");
  
  return notPreserveVar1 + notPreserveVar2 + notPreserveDef;
}

tuple[map[ID,str],map[ID,str]] compRenamings(<Vs,Es>, <Vt,Et>, t, badBindings, str(ID, &T) nameAt) {
  Nsrc = ();
  Nsyn = ();
  
  for (vd <- badBindings<1>) {
    fresh = gensym(nameAt(vd, t), allNames(Vt, t) + Nsrc<1> + Nsyn<1>);
    if (vd in Vs && vd notin Nsrc)
      Nsrc += (vd:fresh) + (v:fresh | v <- Es<0>, Es[v] == vd);
    if (vd notin Vs && vd notin Nsyn) // vd in Vt \ Vs
      Nsyn += (v:fresh | v <- Vt - Vs, nameAt(v, t) == nameAt(vd, t));
  };
  
  //println("Nsrc: <Nsrc>");
  //println("Nsyn: <Nsyn>");

  return <Nsrc,Nsyn>;
}




&T nameFix(type[&T<:node] astType, NameGraph Gs, &T t, NameGraph(&T) resolveT) 
  = x // vvvvv work around Rascal bug.
  when resetFigs(), 
       &T x := nameFix(Gs, t, rename, resolveT, nameAt),
       renderFigs();
  
 
&T nameFix(NameGraph Gs, &T t, &T(&T t, map[ID,str] subst) rename, NameGraph(&T) resolveT, str(ID, &T) nameAt) {
  Gt = resolveT(t);
  //recordNameGraphFig(Gt, t);
  
  //println("Source nodes: <Gs.V>");
  //println("Target nodes: <Gt.V>");
  //println("Source edges: <Gs.E>");
  //println("Target edges: <Gt.E>");
  
  allBadBindings = badBindings(Gs, Gt);
   
  if (allBadBindings == ()) return t;
  
  <Nsrc,Nsyn> = compRenamings(Gs, Gt, t, allBadBindings, nameAt);
  &T t_new = rename(t, Nsrc + Nsyn);
  
  return nameFix(Gs, t_new, rename, resolveT, nameAt);
}
