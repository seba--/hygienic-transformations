module lang::simple::Eval

import lang::simple::Syntax;
import List;

alias Store = map[str, Val];
alias Result = tuple[Store store, Val val];

Result eval(Prog p) = eval(p.defs, head(p.main), ())
  when !isEmpty(p.main);

Result eval(list[Def] defs, val(v), Store store) = <store, v>;

Result eval(list[Def] defs, var(v), Store store) {
  if (v in store)
    return <store, store[v]>;
  else
    return error("UnboundVariable");
}

Result eval(list[Def] defs, assign(v, e), Store store) {
  <store, val> = eval(defs, e, store);
  return <store + (v:val), val>;
}



//Val eval(list[Def] defs, Var v, map[str, Val] store) {
//
//}

//test bool test1(Prog p) {
//  eval(p);
//  return true;
//}