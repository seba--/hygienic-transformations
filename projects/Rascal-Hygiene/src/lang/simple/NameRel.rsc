module lang::simple::NameRel

import lang::simple::AST;
import name::NameGraph;
import name::IDs;

import IO;

alias Scope = map[str, ID];
alias Answer = tuple[NameGraph ng, Scope sc];

Scope collectDefinitions(Prog p) =
  ( def.fsym: getID(def.fsym) | /FDef def := p );

Answer resolveNamesFDef(FDef def, Scope scope) {
  <<V, E>, _> = resolveNamesExp(def.body, scope + (def.fsym : getID(def.fsym)) + (p: getID(p) | p <- def.params));
  return <<V + {getID(def.fsym)} + {getID(p) | p <- def.params},
           E>,
          scope + (def.fsym : getID(def.fsym))>;
}

NameGraph mainResolveNamesExp(Exp e) = 
  resolveNamesExp(e, ())<0>;

Answer resolveNamesExp(var(v), Scope scope) {
  if (v in scope)
  	  return <<{getID(v)}, (getID(v):scope[v])>, scope>;
  	else
  	  return <<{getID(v)}, ()>, scope>;
}   

Answer resolveNamesExp(assign(v, e), Scope scope) {
  <<V,E>, scope2> = resolveNamesExp(e, scope);
  if (v in scope)
  		  return <<V + {getID(v)}, E + (getID(v):scope[v])>, scope2>;
  		else
  		  return <<V + {getID(v)}, E>, scope2>;
}

Answer resolveNamesExp(call(v, args), Scope scope) {
  V = {getID(v)};
  E = ();
  if (v in scope)
    E = (getID(v):scope[v]);
  for (e <- args) {
    <<V2,E2>, _> = resolveNamesExp(e, scope);
    V += V2;
    E += E2;
  }
  return <<V,E>, scope>;
}

Answer resolveNamesExp(let(x, e, body), Scope scope) {
  <<V1, E1>, _> = resolveNamesExp(e, scope);
  <<V2, E2>, _> = resolveNamesExp(body, scope + (x : getID(x)));
  return <<V1 + V2 + {getID(x)}, E1 + E2>, scope>;
}

default Answer resolveNamesExp(Exp e, Scope scope) {
  <V,E> = <{},()>;
  for (Exp e2 <- e) {
    <<V2,E2>, scope> = resolveNamesExp(e2, scope);
    <V,E> = <V + V2,E + E2>;
  }
  return <<V,E>, scope>;
}

NameGraph resolveNames(Prog p) {
  scope = collectDefinitions(p);
  
  <dV,dE> = <{},()>;
  for (d <- p.fdefs) {
    <<V2,E2>, scope> = resolveNamesFDef(d, scope);
    <dV,dE> = <dV + V2,dE + E2>;
  }
  <mV,mE> = <{},()>;
  for (e <- p.main) {
    <<V2,E2>, scope> = resolveNamesExp(e, scope);
    <mV,mE> = <mV + V2,mE + E2>;
  }
  
  return <dV + mV,dE + mE>;
}

