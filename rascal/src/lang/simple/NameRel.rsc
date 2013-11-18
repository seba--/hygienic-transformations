module lang::simple::NameRel

import lang::simple::AST;
import name::Relation;
import name::Names;

import IO;

alias Scope = map[str, ID];
alias Answer = tuple[NameGraph ng, Scope sc];

Scope collectDefinitions(Prog p) =
  ( def.fsym: getID(def.fsym) | /FDef def := p );

Answer resolveNamesVDef(vdef(str name, Exp exp), Scope scope) {
  <<V, E, N>, _> = resolveNamesExp(exp, scope);
  return <<V + {getID(name)}, E, N + (getID(name) : name)>,
          scope + (name : getID(name))>;
}

Answer resolveNamesFDef(FDef def, Scope scope) {
  <<V, E, N>, _> = resolveNamesExp(def.body, scope + (p: getID(p) | p <- def.params));
  return <<V + {getID(def.fsym)} + {getID(p) | p <- def.params},
           E,
           N + (getID(def.fsym):def.fsym) + (getID(p):p | p <- def.params)>,
          scope + (def.fsym : getID(def.fsym))>;
}

NameGraph mainResolveNamesExp(Exp e) = 
  resolveNamesExp(e, ())<0>;

Answer resolveNamesExp(var(v), Scope scope) {
  if (v in scope)
  	  return <<{getID(v)}, (getID(v):scope[v]), ()>, scope>;
  	else
  	  return <<{getID(v)}, (), ()>, scope>;
}   

Answer resolveNamesExp(assign(v, e), Scope scope) {
  <<V,E,N>, scope2> = resolveNamesExp(e, scope);
  if (v in scope)
  		  return <<V + {getID(v)}, E + (getID(v):scope[v]), N>, scope2>;
  		else
  		  return <<V + {getID(v)}, E, N>, scope2>;
}

Answer resolveNamesExp(call(v, args), Scope scope) {
  V = {getID(v)};
  E = ();
  if (v in scope)
    E = (getID(v):scope[v]);
  N = ();
  for (e <- args) {
    <<V2,E2,N2>, _> = resolveNamesExp(e, scope);
    V += V2;
    E += E2;
    N += N2;
  }
  return <<V,E,N>, scope>;
}

Answer resolveNamesExp(block(list[VDef] vini, Exp body), Scope scope) {
  <V, E, N> = <{}, (), ()>;
  lscope = scope;
  for (vdef <- vini) {
    <<dV, dE, dN>, _> = resolveNamesVDef(vdef, lscope);
    <V, E, N> = <V + dV, E + dE, N + dN>;
    lscope = lscope + (vdef.name : getID(vdef.name));
  }
  <<dV, dE, dN>, _> = resolveNamesExp(body, lscope);
  return <<V + dV, E + dE, N + dN>, scope>;
}

default Answer resolveNamesExp(Exp e, Scope scope) {
  <V,E,N> = <{},(),()>;
  for (Exp e2 <- e) {
    <<V2,E2,N2>, scope> = resolveNamesExp(e2, scope);
    <V,E,N> = <V + V2,E + E2, N + N2>;
  }
  return <<V,E,N>, scope>;
}

NameGraph resolveNames(Prog p) {
  scope = collectDefinitions(p);
  
  <dV,dE,dN> = <{},(),()>;
  for (d <- p.fdefs) {
    <<V2,E2,N2>, scope> = resolveNamesFDef(d, scope);
    <dV,dE,dN> = <dV + V2,dE + E2,dN + N2>;
  }
  <mV,mE,mN> = <{},(),()>;
  for (e <- p.main) {
    <<V2,E2,N2>, scope> = resolveNamesExp(e, scope);
    <mV,mE,mN> = <mV + V2,mE + E2,mN + N2>;
  }
  
  return <dV + mV,dE + mE, dN + mN>;
}

