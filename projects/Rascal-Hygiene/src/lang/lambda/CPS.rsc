module lang::lambda::CPS

import lang::lambda::Syntax;

int newvarCount = 0;
void resetNewvar() {newvarCount = 0;}
str newvar() = newvar("x");
str newvar(str base) {
  v = "<base><newvarCount>";
  newvarCount += 1;
  return v;
}

Exp CPS1(lambda(x, body)) {
  k = newvar("k");
  return lambda(x, lambda(k, CPS1(body, var(k))));
}
Exp CPS1(nat(n)) = nat(n);

Exp CPS1(var(x), k) = app(k, var(x));
Exp CPS1(nat(n), k) = app(k, nat(n));
Exp CPS1(lambda(x, body), k) = app(k, CPS1(lambda(x, body))); 
Exp CPS1(plus(e1, e2), k) {
  v1 = newvar(); 
  v2 = newvar();
  return 
    CPS1(e1, lambda(v1, 
      CPS1(e2, lambda(v2, 
        app(k, plus(var(v1), var(v2)))))));
}
Exp CPS1(app(e1, e2), k) { 
  v1 = newvar(); 
  v2 = newvar();
  return 
    CPS1(e1, lambda(v1, 
      CPS1(e2, lambda(v2, 
        app(app(var(v1), var(v2)), k)))));
}


Exp CPS2(lambda(x, body)) {
  k = newvar("k");
  return lambda(x, lambda(k, CPS2(body, Exp (e){return app(var(k), e);})));
}
Exp CPS2(nat(n)) = nat(n);
Exp CPS2(var(v)) = var(v);

Exp CPS2(var(x), k) = k(var(x));
Exp CPS2(nat(n), k) = k(nat(n));
Exp CPS2(lambda(x, body), k) = k(CPS2(lambda(x, body))); 
Exp CPS2(plus(e1, e2), k) {
  return 
    CPS2(e1, Exp (v1){
      return CPS2(e2, Exp (v2) {
        return k(plus(v1, v2));
      });
    });
}
Exp CPS2(app(e1, e2), k) { 
  rv = newvar(); 
  cont = lambda(rv, k(rv));
  return 
    CPS2(e1, Exp (v1){
      return CPS2(e2, Exp (v2) {
        return app(app(v1, v2), cont);
      });
    });
}


