module lang::simple::Eval

import lang::simple::Syntax;
import List;

alias Store = map[str, Val];
alias Result = tuple[Store store, Val val];

Result eval(Prog p) = eval(p.defs, head(p.main), ())
  when !isEmpty(p.main);

Result eval(list[Def] defs, val(v), Store store) = <store, v>;

Result eval(list[Def] defs, evar(var(nom)), Store store) {
  if (nom in store)
    return <store, store[nom]>;
  else
    return error("UnboundVariable");
}

Result eval(list[Def] defs, assign(var(nom), e), Store store) {
  <store, val> = eval(defs, e, store);
  return <store + (nom:val), val>;
}



//Val eval(list[Def] defs, Var v, map[str, Val] store) {
//
//}

//test bool test1(Prog p) {
//  eval(p);
//  return true;
//}
