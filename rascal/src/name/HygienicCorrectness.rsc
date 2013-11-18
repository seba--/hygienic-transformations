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

tuple[Edges,Edges] unhygienicLinks(NameGraph Gs, NameGraph Gt) {
  <Vs,Es,_> = Gs;
  <Vt,Et,_> = Gt;
  Vsyn = Vt - Vs;
  notPreserveSourceBinding =    (u:d | u <- Vs & Vt, u in Et, d:=Et[u], u in Es ? Es[u] != d : u != d);
  notPreventCrossReferences =   (u:d | <u,d> <- Et<0,1>, u in Vs && d notin Vs || u in Vsyn && d notin Vsyn);
  
  //notPreserveDefinitionScope =  (u:Et[u] | d <- Vs & Vt, u <- Et, Et[u] == d, u in Es ? Es[u] != d : d != u);
  //notSafeDefinitionReferences = (u:Et[u] | u <- Vs & Vt, u notin Es, u in Et, Et[u] != u);
  
  //println("not preserve source binding: <notPreserveSourceBinding>");
  //println("not prevent cross references: <notPreventCrossReferences>");
  
  //println("not preserve definition scope: <notPreserveDefinitionScope>");
  //println("not safe definition references: <notSafeDefinitionReferences>");
  
  
  return <notPreserveSourceBinding, notPreventCrossReferences>;
}

bool isCompiledHygienically(NameGraph Gs, NameGraph Gt) =
  unhygienicLinks(Gs, Gt) == <(),()> && sourceNotPreserved(Gs, Gt) == () && synthesizedCaptured(Gs,Gt) == ();







