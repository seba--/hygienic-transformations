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

Result eval(Sig sig, call(var(nom), list[Exp] opds), Env env) {
  if (nom in env) {
    define(_, pars, bod) = env[nom];
    nopds = size(opds);
    npars = size(pars);
    if (nopds == npars) {
      /* We extend the empty environment with parameter-argument bindings.
       * In other words, we forbit global variables and force every global
       * fuction to be a supercombinator! */
      lenv = (par : eval(sig, opd, env).val | par <- pars, opd <- opds);
      return eval(sig, bod, lenv);
    }
    else
      return error("Wrong number of arguments: <nopds> instead of <npars>");
  }
  else
    return error("Undefined function: " + nom);
}

Result eval(Sig sig, cond(Exp cnd, Exp csq, Exp alt), Env env) {
  // Side effects by evaluating the condition is supported.
  <env, val> = eval(sig, cnd, env);
  return eval(sig, val != nat(0) ? csq : alt, env);
}

Result eval(Sig sig, plus(Exp lhs, Exp rhs), Env env) =
  // Side effects by evaluating operands of `plus` is disallowed.
  <env, eval(sig, lhs, env).val + eval(sig, rhs, env).val>;

Result eval(Sig sig, seq(Exp exp1, Exp exp2), Env env) {
  <env, _> = eval(sig, exp1, env);
  return eval(sig, exp2, env);
}

Result eval(Sig sig, eq(Exp lhs, Exp rhs), Env env) =
  // Side effects by evaluating operands of `eq` is disallowed.
  <env, nat(eval(sig, lhs, env) == eval(sig, rhs, env) ? 1 : 0)>;

Result eval(Sig sig, block(list[Var] vars, Exp exp), Env env) {
  // Local variables are automatically initialized to 0.
  env = env + (nom : nat(0) | var(nom) <- vars);
  return eval(sig, exp, env);
}

