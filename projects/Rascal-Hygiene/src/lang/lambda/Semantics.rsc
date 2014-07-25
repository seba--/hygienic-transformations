module lang::lambda::Semantics

import lang::lambda::Syntax;
import lang::lambda::Names;

import name::NameFix;

Exp norm(plus(e1, e2)) {
  Exp n1 = norm(e1);
  Exp n2 = norm(e2);
  if (nat(v1) := n1 && nat(v2) := n2)
    return nat(v1 + v2);
  return plus(n1, n2);
}
Exp norm(app(e1, e2)) {
  if (lambda(v, body):= norm(e1))
    return norm(safeSubst(body, v, e2));
  return app(e1, e2);
}
default Exp norm(e) = e;


Exp subst(var(v), v, e) = e;
Exp subst(var(v), w, e) = var(v);
Exp subst(nat(n), w, e) = nat(n);
Exp subst(plus(e1, e2), w, e) = plus(subst(e1, w, e), subst(e2, w, e));
Exp subst(app(e1, e2), w, e) = app(subst(e1, w, e), subst(e2, w, e));
Exp subst(lambda(v, body), v, e) = lambda(v, body);
Exp subst(lambda(v, body), w, e) = lambda(v, subst(body, w, e));


Exp safeSubst(e1, w, e) {
  G1 = resolve(e1);
  e2 = subst(e1, w, e);
  return nameFix(#Exp, G1, e2, resolve);
}
