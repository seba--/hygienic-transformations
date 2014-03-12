module name::NameFix

import name::IDs;
import name::NameGraph;
import name::Gensym;
import name::Figs;

import IO;
import Map;
import String;

Edges findCapture(<Vs,Es>, <Vt,Et>) {
  notPreserveVar1 =    (v:Et[v] | v <- Et<0>, v in Vs, v in Es, Es[v] != Et[v]);
  notPreserveVar2 =    (v:Et[v] | v <- Et<0>, v in Vs, v notin Es, v != Et[v]);
  notPreserveDef  =    (v:Et[v] | v <- Et<0>, v notin Vs, Et[v] in Vs);
  
  //println("not preserve source vars 1: <notPreserveVar1>");
  //println("not preserve source vars 2: <notPreserveVar2>");
  //println("not preserve source defs  : <notPreserveDef>");
  
  return notPreserveVar1 + notPreserveVar2 + notPreserveDef;
}

tuple[map[ID,str],map[ID,str]] compRenamings(<Vs,Es>, <Vt,Et>, t, findCapture, str(ID, &T) nameAt) {
  Nsrc = ();
  Nsyn = ();
  
  for (vd <- findCapture<1>) {
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

&T rename(&T t, map[ID,str] subst) {
  // use Rascal's generic visitors (http://tutor.rascal-mpl.org/Rascal/Rascal.html#/Rascal/Expressions/Visit/Visit.html)
  return visit (t) {
    case str x => setID(subst[getID(x)], getID(x)) // use setID to preserve ID of original variable
      when getID(x) in subst
  };
}


/*
  Arguments:
    * Gs is the name graph of the source program
    * t is the target program
    * resolveT : &T -> NameGraph does name-analysis for the target language
*/
&T nameFix(type[&T<:node] astType, NameGraph Gs, &T t, NameGraph(&T) resolveT) = x
  when resetFigs(), // for displaying name graphs 
       &T x := nameFix(Gs, t, resolveT, rename, nameAt),
       renderFigs(); // for displaying name graphs
  

/*
  Arguments:
    * Gs is the name graph of the source program
    * t is the target program
    * resolveT : &T -> NameGraph is a name-analysis function for the target language
    * renameF : (&T, map[ID,str]) -> &T is a rename function for the target language
    * nameAtF : (ID, &T) -> str retrieves the name at a certain position for the target language
    
  The last two arguments `renameF` and `nameAt` are only used to support name-fixing
  for string-based program representations. See the use of name-fix in NameFixString
  for details. 
*/
&T nameFix(NameGraph Gs, &T t, NameGraph(&T) resolveT,
           &T(&T t, map[ID,str] subst) renameF, str(ID, &T) nameAtF) {

  Gt = resolveT(t);
  //recordNameGraphFig(Gt, t);  // for displaying name graphs
  
  //println("Source nodes: <Gs.V>");
  //println("Target nodes: <Gt.V>");
  //println("Source edges: <Gs.E>");
  //println("Target edges: <Gt.E>");
  
  capture = findCapture(Gs, Gt);
   
  if (capture == ()) return t;
  
  <Nsrc,Nsyn> = compRenamings(Gs, Gt, t, capture, nameAtF);
  &T t_new = renameF(t, Nsrc + Nsyn);
  
  return nameFix(Gs, t_new, resolveT, rename, nameAtF);
}
