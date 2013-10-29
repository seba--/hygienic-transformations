module lang::simple::Eval

import lang::simple::AST;
import List;

alias Store = map[str, Val];
alias Result = tuple[Store store, Val val];

Result eval(Prog p) = eval(p.sig, head(p.main), ())
  when !isEmpty(p.main);

Result eval(Sig sig, val(v), Store store) = <store, v>;

Result eval(Sig sig, evar(var(nom)), Store store) {
  if (nom in store)
    return <store, store[nom]>;
  else
    return error("Unbound variable: " + nom);
}

Result eval(Sig sig, assign(var(nom), e), Store store) {
  <store, val> = eval(sig, e, store);
  return <store + (nom:val), val>;
}



//Val eval(list[Def] defs, Var v, map[str, Val] store) {
//
//}

//test bool test1(Prog p) {
//  eval(p);
//  return true;
//}
