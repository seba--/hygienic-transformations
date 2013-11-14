module lang::locfun::NameRel


import lang::simple::NameRel;
import lang::locfun::AST;


Answer resolveNamesExp(block(list[FDef] fini, Exp body), Scope scope) {
  <V, E, N> = <{}, (), ()>;
  lscope = scope;
  for (fdef <- fini) {
    <<dV, dE, dN>, _> = resolveNamesFDef(fdef, lscope);
    <V, E, N> = <V + dV, E + dE, N + dN>;
    lscope = lscope + (fdef.fsym : getID(fdef.fsym));
  }
  <<dV, dE, dN>, _> = resolveNamesExp(body, lscope);
  return <<V + dV, E + dE, N + dN>, scope>;
}

