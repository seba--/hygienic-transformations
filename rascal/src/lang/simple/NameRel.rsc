module lang::simple::NameRel

import lang::simple::AST;
import name::Relation;

import IO; 

//anno int FDef@location;
//anno int Var@location;
//
//Prog makeUniqueIds(Prog p) {
//  int n = 0;
//  return visit (p) {
//    case Var v : {
//      n += 1;
//      insert v[@location=n];
//    }
//  }
//}

alias Scope = map[str, loc];
alias Answer = tuple[NameGraph ng, Scope sc];

Answer resolveNamesDef(FDef def, Scope scope) {
  <<V, E, N>, _> = resolveNamesExp(def.body, scope + (p.name:p@location | p <- def.params));
  return <<V + def.fsym@location + {p@location | p <- def.params},
           E,
           N + (def.fsym@location:def.fsym.name) + (p@location:p.name | p <- def.params)>,
          scope + (def.fsym.name : def.fsym@location)>;
}

Answer resolveNamesExp(var(v), Scope scope) =
  <<{v@location}, (v@location:scope[v.name]), ()>, scope>   
  when v.name in scope;
//NameRel resolveNames(var(v), Scope scope) =
//  {<v.name, UNBOUND>}
//  when v.name notin scope;


Answer resolveNamesExp(vardecl(v, e), Scope scope) {
  scope = scope + (v.name:v@location);
  <<V,E,N>, scope> = resolveNamesExp(e, scope);
  return <<V + {v@location}, E, N + (v@location:v.name)>, scope>;
}

Answer resolveNamesExp(assign(v, e), Scope scope) {
  <<V,E,N>, scope2> = resolveNamesExp(e, scope);
  if (v.name notin scope)
    throw "Unbound variable <v.name> at <v@location>.";
  return <<V + {v@location}, E + (v@location:scope[v.name]), N>, scope2>;
}

Answer resolveNamesExp(call(v, args), Scope scope) {
  if (v.name notin scope)
    throw "Unbound variable <v.name> at <v@location>.";

  V = {v@location};
  E = (v@location:scope[v.name]);
  N = ();
  for (e <- args) {
    <<V2,E2,N2>, _> = resolveNamesExp(e, scope);
    V += V2;
    E += E2;
    N += N2;
  }
  return <<V,E,N>, scope>;
}

// TODO: block scoping
Answer resolveNamesExp(block(Exp exp), Scope scope) {
  <ng, _> = resolveNamesExp(exp, scope);
  return <ng, scope>;
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
  scope = ();
  
  <dV,dE,dN> = <{},(),()>;
  for (d <- p.fdefs) {
    <<V2,E2,N2>, scope> = resolveNamesDef(d, scope);
    <dV,dE,dN> = <dV + V2,dE + E2,dN + N2>;
  }
  <mV,mE,mN> = <{},(),()>;
  for (e <- p.main) {
    <<V2,E2,N2>, scope> = resolveNamesExp(e, scope);
    <mV,mE,mN> = <mV + V2,mE + E2,mN + N2>;
  }
  
  return <dV + mV,dE + mE, dN + mN>;
}

