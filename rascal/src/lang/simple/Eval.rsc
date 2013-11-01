module lang::simple::Eval


import lang::simple::AST;
import List;
import util::Maybe;


alias Env = map[str, Val];

alias Result = tuple[Env env, Val val];


Result eval(Prog prg) = eval(prg.fdefs, head(prg.main), ())
  when !isEmpty(prg.main);

Result eval(FDefs fdefs, val(Val v), Env env) = <env, v>;

Result eval(FDefs fdefs, var(sym(str s)), Env env) {
  if (s in env)
    return <env, env[s]>;
  else
    return <env, error("Unbound variable: " + s)>;
}

Result eval(FDefs fdefs, assign(sym(str s), exp), Env env) {
  <env, v> = eval(fdefs, exp, env);
  return <env + (s : v), v>;
}

Result eval(FDefs fdefs, call(sym(str s), list[Exp] opds), Env env) {
  switch (lookup(s, fdefs)) {
    case nothing(): return <env, error("Undefined function: " + s)>;
    case just(fdef(_, pars, bod)): {
      nopds = size(opds);
      npars = size(pars);
      if (nopds == npars) {
        /* We extend the empty environment with parameter-argument bindings.
         * In other words, we forbid global variables and require every
         * top-level fuction to be a supercombinator. */
        lenv = (s : eval(fdefs, opd, env).val | <sym(str s), opd> <- zip(pars, opds));
        return eval(fdefs, bod, lenv);
      }
      else
        return <env, error("Wrong number of arguments: <nopds> instead of <npars>")>;
    }
  }
}

Result eval(FDefs fdefs, cond(Exp cnd, Exp csq, Exp alt), Env env) {
  // Side effects by evaluating the condition is supported.
  <env, nat(n)> = eval(fdefs, cnd, env);
  return eval(fdefs, n != 0 ? csq : alt, env);
}

Result eval(FDefs fdefs, plus(Exp exp1, Exp exp2), Env env) {
  // Side effects by evaluating operands of `plus` is disallowed.
  nat(n1) = eval(fdefs, exp1, env).val;
  nat(n2) = eval(fdefs, exp2, env).val;
  return <env, nat(n1 + n2)>;
}

Result eval(FDefs fdefs, seq(Exp exp1, Exp exp2), Env env) {
  <env, _> = eval(fdefs, exp1, env);
  return eval(fdefs, exp2, env);
}

Result eval(FDefs fdefs, eq(Exp exp1, Exp exp2), Env env) {
  // Side effects by evaluating operands of `eq` is disallowed.
  nat(n1) = eval(fdefs, exp1, env).val;
  nat(n2) = eval(fdefs, exp2, env).val;
  return <env, nat(n1 == n2? 1 : 0)>;
}

Result eval(FDefs fdefs, block(list[VDef] vdefs, Exp exp), Env env) {
  for (vdef(sym(str s), Exp exp) <- vdefs)
    env = env + (s : eval(fdefs, exp, env).val);
  return eval(fdefs, exp, env);
}


Maybe[FDef] lookup(str s, FDefs fdefs) {
  switch (fdefs) {
    case []: return nothing();
    case [fd:fdef(sym(t), _, _), *fds]: {
      if (t == s)
        return just(fd);
      else
        return lookup(s, fds);
    }
  }
}
