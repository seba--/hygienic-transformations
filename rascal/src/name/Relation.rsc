module name::Relation

import IO;
import Set;

import name::Names;

alias Edge = tuple[ID use, ID def];
alias Edges = map[ID use, ID def];
alias NameGraph = tuple[set[ID] V, Edges E, map[ID v, str name] N];

set[ID] synthesizedNodes(NameGraph Gs, NameGraph Gt) = Gt.V - Gs.V;


ID refOf(ID n, Edges refs) = refs[n];
ID refOf(str n, Edges refs) = refs[getID(n)];

ID refOf(ID n, NameGraph G) = refOf(n, G.E);
ID refOf(str n, NameGraph G) = refOf(getID(n), G.E); 

str nameOf(ID n, NameGraph G) {
  if (n in G.N)
    return G.N[n];
  if (refOf(n, G.E) in G.N)
    return G.N[refOf(n, G.E)];
  throw "Node <n> has no name in <G.N>";
}

set[str] namesOf(NameGraph G) = G.N<1>;

set[ID] idsOf(NameGraph G) = G.N<0>;

set[ID] defsOf(NameGraph G) = G.E.def;

set[ID] usesOf(NameGraph G) = G.E.use;


NameGraph makeGraph(rel[str name,ID l] names, rel[ID use, ID def] refs) {
  nodes = names<1>;
  N = names<1,0>;
  
  if (size(refs<0>) != size(refs))
    throw "NameGraph requires unique mapping from name use to name def, but got <refs>";
  if (size(N<0>) != size(N))
    throw "NameGraph requires unique mapping from node labels to names, but got <N>";
  
  return <nodes, ( u:d | <u,d> <- refs ), ( v:name | <v,name> <- N )>;
}

NameGraph union(NameGraph g1, NameGraph g2) =
  <g1.V + g2.V, g1.E + g2.E, g1.N + g2.N>
   when g1.E<0> & g2.E<0> == {} && g1.N<0> & g2.N<0> == {};
