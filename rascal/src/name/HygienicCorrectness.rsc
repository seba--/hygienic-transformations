module name::HygienicCorrectness

import name::Relation;

import IO;

set[&T] delta(set[&T] s1, set[&T] s2) =
  { x | x <- s1, x notin s2 } + { y | y <- s2, y notin s1 };

@doc {
Uses from the source point to the same definition in the target.
@return set of illegal links.
}
Edges sourceNotPreserved(NameGraph Gs, NameGraph Gt) {
  // Reason for condition: u != d
  //   A transformation can always duplicate a source name to modularize
  //   the resulting program by splitting the corresponding source-language
  //   concept into a definition and one or more references to this definition.
  return (u:d | <u,d> <- Gt.E<0,1>, u in Gs.V, u != d, u in Gs.E ? Gs.E[u] != d : true);
}


@doc {
Synthesized names never point to source labels.
@return set of illegal links.
}
Edges synthesizedCaptured(NameGraph Gs, NameGraph Gt) {
  return (u:d | <u,d> <- Gt.E<0,1>, u notin Gs.V, d in Gs.V);
}

tuple[Edges,Edges,Edges] unhygienicLinks(NameGraph Gs, NameGraph Gt) {
  <Vs,Es,_> = Gs;
  <Vt,Et,_> = Gt;
  Vsyn = Vt - Vs;
  
  notPreserveVar1 =    (u:Et[u] | u <- Vs & Vt, u in Et, u in Es, Es[u] != Et[u]);
  notPreserveVar2 =    (u:Et[u] | u <- Vs & Vt, u in Et, u notin Es, u != Et[u]);
  notPreserveDef  =    (u:Et[u] | u <- Vt, u in Et, u notin Vs, Et[u] in Vs);
  
  println("not preserve source vars 1: <notPreserveVar1>");
  println("not preserve source vars 2: <notPreserveVar2>");
  println("not preserve source defs  : <notPreserveDef>");
  
  return <notPreserveVar1, notPreserveVar2, notPreserveDef>;
}

bool isCompiledHygienically(NameGraph Gs, NameGraph Gt) =
  unhygienicLinks(Gs, Gt) == <(),(),()> && sourceNotPreserved(Gs, Gt) == () && synthesizedCaptured(Gs,Gt) == ();







