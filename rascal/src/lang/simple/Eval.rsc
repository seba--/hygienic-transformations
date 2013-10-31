module lang::simple::Eval


import lang::simple::AST;
import List;
import util::Maybe;


alias Env = map[str, Val];

alias Result = tuple[Env env, Val val];


Result eval(Prog prg) = eval(prg.sig, head(prg.main), ())
  when !isEmpty(prg.main);

Result eval(Sig sig, val(Val v), Env env) = <env, v>;

Result eval(Sig sig, evar(var(str s)), Env env) {
  if (s in env)
    return <env, env[s]>;
  else
    return <env, error("Unbound variable: " + s)>;
}

Result eval(Sig sig, assign(var(str s), exp), Env env) {
  <env, v> = eval(sig, exp, env);
  return <env + (s : v), v>;
}

Result eval(Sig sig, call(var(str s), list[Exp] opds), Env env) {
  switch (lookup(s, sig)) {
    case nothing(): return <env, error("Undefined function: " + s)>;
    case just(fdef(_, pars, bod)): {
      nopds = size(opds);
      npars = size(pars);
      if (nopds == npars) {
        /* We extend the empty environment with parameter-argument bindings.
         * In other words, we forbid global variables and require every
         * top-level fuction to be a supercombinator. */
        lenv = (s : eval(sig, opd, env).val | <var(str s), opd> <- zip(pars, opds));
        return eval(sig, bod, lenv);
      }
      else
        return <env, error("Wrong number of arguments: <nopds> instead of <npars>")>;
    }
  }
}

Result eval(Sig sig, cond(Exp cnd, Exp csq, Exp alt), Env env) {
  // Side effects by evaluating the condition is supported.
  <env, nat(n)> = eval(sig, cnd, env);
  return eval(sig, n != 0 ? csq : alt, env);
}

Result eval(Sig sig, plus(Exp exp1, Exp exp2), Env env) {
  // Side effects by evaluating operands of `plus` is disallowed.
  nat(n1) = eval(sig, exp1, env).val;
  nat(n2) = eval(sig, exp2, env).val;
  return <env, nat(n1 + n2)>;
}

Result eval(Sig sig, seq(Exp exp1, Exp exp2), Env env) {
  <env, _> = eval(sig, exp1, env);
  return eval(sig, exp2, env);
}

Result eval(Sig sig, eq(Exp exp1, Exp exp2), Env env) {
  // Side effects by evaluating operands of `eq` is disallowed.
  nat(n1) = eval(sig, exp1, env).val;
  nat(n2) = eval(sig, exp2, env).val;
  return <env, nat(n1 == n2? 1 : 0)>;
}

Result eval(Sig sig, block(list[Var] vars, Exp exp), Env env) {
  // Local variables are initialized to `0` by default.
  env = env + (s : nat(0) | var(str s) <- vars);
  return eval(sig, exp, env);
}


Maybe[FDef] lookup(str s, Sig sig) {
  switch (sig) {
    case []: return nothing();
    case [def:fdef(var(t), _, _), *defs]: {
      if (t == s)
        return just(def);
      else
        return lookup(s, defs);
    }
  }
}
