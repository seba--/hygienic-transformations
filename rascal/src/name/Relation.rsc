module name::Relation

import IO;
import Set;

//alias Node = loc;
alias Reference = tuple[loc use, loc def];
alias NameGraph = tuple[set[loc] V, map[loc use, loc def] E, rel[loc, str] N];

set[loc] synthesizedLabels(NameGraph Gs, NameGraph Gt) = Gt.V - Gs.V;

loc refOf(loc n, set[tuple[loc use, loc def]] refs) = x when {x} := refs[n];
loc refOf(loc n, NameGraph G) = refOf(n, G.E); 

str nameOf(loc n, NameGraph G) {
  if ({name} := G.N[n])
    return name;
  if ({name} := G.N[refOf(n, G.E)])
    return name;
  throw "loc <n> has no name in <G.N>";
}

set[str] names(NameGraph G) = G.N<1>;

NameGraph makeGraph(rel[str name,loc l] names, rel[loc use, loc def] refs) {
  nodes = names<1>;
  N = names<1,0>;
  
  if (size(refs<0>) != size(refs))
    throw "NameGraph requires unique mapping from name use to name def, but got <refs>";
  if (size(N<0>) != size(N))
    throw "NameGraph requires unique mapping from node labels to names, but got <N>";
  
  return <nodes, refs, N>;
}


