module lang::simple::Eval

import lang::simple::AST;
import List;

alias Env = map[str, Val];
alias Result = tuple[Env env, Val val];

Result eval(Prog p) = eval(p.sig, head(p.main), ())
  when !isEmpty(p.main);

Result eval(Sig sig, val(v), Env env) = <env, v>;

Result eval(Sig sig, evar(var(nom)), Env env) {
  if (nom in env)
    return <env, env[nom]>;
  else
    return error("Unbound variable: " + nom);
}

Result eval(Sig sig, assign(var(nom), e), Env env) {
  <env, val> = eval(sig, e, env);
  return <env + (nom:val), val>;
}




//Val eval(list[Def] defs, Var v, map[str, Val] store) {
//
//}

//test bool test1(Prog p) {
//  eval(p);
//  return true;
//}
