module name::Relation

import IO;
import Set;

//alias Node = loc;
alias Edge = tuple[loc use, loc def];
alias Edges = map[loc use, loc def];
alias NameGraph = tuple[set[loc] V, Edges E, map[loc v, str name] N];

set[loc] synthesizedNodes(NameGraph Gs, NameGraph Gt) = Gt.V - Gs.V;

loc refOf(loc n, Edges refs) = refs[n];
loc refOf(loc n, NameGraph G) = refOf(n, G.E); 

str nameOf(loc n, NameGraph G) {
  if (n in G.N)
    return G.N[n];
  if (refOf(n, G.E) in G.N)
    return G.N[refOf(n, G.E)];
  throw "Node <n> has no name in <G.N>";
}

set[str] namesOf(NameGraph G) = G.N<1>;

NameGraph makeGraph(rel[str name,loc l] names, rel[loc use, loc def] refs) {
  nodes = names<1>;
  N = names<1,0>;
  
  if (size(refs<0>) != size(refs))
    throw "NameGraph requires unique mapping from name use to name def, but got <refs>";
  if (size(N<0>) != size(N))
    throw "NameGraph requires unique mapping from node labels to names, but got <N>";
  
  return <nodes, ( u:d | <u,d> <- refs ), ( v:name | <v,name> <- N )>;
}


