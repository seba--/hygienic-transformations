module name::Relation

import IO;
import Set;

import name::Names;

alias Edge = tuple[ID use, ID def];
alias Edges = map[ID use, ID def];
alias NameGraph = tuple[set[ID] V, Edges E];

set[ID] synthesizedNodes(NameGraph Gs, NameGraph Gt) = Gt.V - Gs.V;


ID refOf(ID n, Edges refs) = refs[n];
ID refOf(str n, Edges refs) = refs[getID(n)];

ID refOf(ID n, NameGraph G) = refOf(n, G.E);
ID refOf(str n, NameGraph G) = refOf(getID(n), G.E); 

bool isRefOf(str u, str d, NameGraph G) {
  uid = getID(u);
  did = getID(d);
  return uid in G.E && G.E[uid] == did;
}

bool isFree(str v, NameGraph G) = getID(v) notin G.E;

str nameAt(ID n, &T t) = 
  visit(t) {
    case str x: 
      if (getID(x) == n)
        return x;
  };

//set[str] namesOf(NameGraph G) = G.N<1>;

//set[ID] idsOf(NameGraph G) = G.N<0>;
set[ID] idsOf(&T t) = ({} | it + {getID(x)} | /str x <- t);

set[ID] defsOf(NameGraph G) = G.E.def;

set[ID] usesOf(NameGraph G) = G.E.use;

rel[ID, str] piOf(NameGraph G, &T t) 
  = { <getID(s), s> | /str s := t, getID(s) in G.V };

set[str] allNames(Vt, t) {
  ss = {};
  visit(t) {
    case str s: ss += s; 
  };
  return ss;
}

NameGraph makeGraph(rel[str name,ID l] names, rel[ID use, ID def] refs) {
  nodes = names<1>;
  N = names<1,0>;
  
  if (size(refs<0>) != size(refs))
    throw "NameGraph requires unique mapping from name use to name def, but got <refs>";
  if (size(N<0>) != size(N))
    throw "NameGraph requires unique mapping from node labels to names, but got <N>";
  
  return <nodes, ( u:d | <u,d> <- refs )>;
}

NameGraph union(NameGraph g1, NameGraph g2) =
  <g1.V + g2.V, g1.E + g2.E>
   when g1.E<0> & g2.E<0> == {};
