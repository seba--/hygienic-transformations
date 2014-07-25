module lang::lambda::Equal

import lang::lambda::Syntax;
import lang::lambda::Names;
import name::NameGraph;
import name::IDs;

bool equal(Exp e1, Exp e2) {
  <V1,E1> = resolve(e1);
  <V2,E2> = resolve(e2);
  G = <V1+V2,E1+E2>;
  return equal(e1, e2, G);
}

bool equal(var(v1), var(v2), NameGraph G) {
  id1 = getID(v1);
  id2 = getID(v2);
  return id1 notin G.E && id2 notin G.E || id1 in G.E && id2 in G.E && G.E[id1] == G.E[id2];
}

bool equal(nat(n), nat(n), G) = true;

bool equal(plus(e11, e12), plus(e21, e22), G) =
  equal(e11, e21, G) && equal(e12, e22, G);

bool equal(app(e11, e12), app(e21, e22), G) =
  equal(e11, e21, G) && equal(e12, e22, G);

bool equal(lambda(v1, e1), lambda(v2, e2), NameGraph G) {
  id1 = getID(v1);
  id2 = getID(v2);
  E2 = (ref:id1 | <ref, dec> <- G.E<0,1>, dec == id2);
  G2 = <G.V, G.E + E2>;
  return equal(e1, e2, G2);
}

default bool equal(e1, e2, G) = false;
