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

map[str,loc] collectDefinitions(Prog p) =
  ( def.name.name:def.name@location | /FDef def := p );

NameGraph resolveNames(FDef def, map[str,loc] scope) {
  <V, E, N> = resolveNames(def.body, scope + (p.name:p@location | p <- def.params));
  return <V + def.name@location + {p@location | p <- def.params}, 
          E,
          N + (def.name@location:def.name.name) + (p@location:p.name | p <- def.params)>;
}

NameGraph resolveNames(evar(v), map[str,loc] scope) = 
  <{v@location}, (v@location:scope[v.name]), ()>
  when v.name in scope;
//NameRel resolveNames(evar(v), map[str,loc] scope) = 
//  {<v.name, UNBOUND>}
//  when v.name notin scope;


NameGraph resolveNames(assign(v, e), map[str,loc] scope) {
  if (v.name in scope)
    scope2 = scope;
  else
    scope2 = scope + (v.name:v@location);
  
  <V,E,N> = resolveNames(e, scope2);
  
  if (v.name in scope)
    return <V + {v@location}, E + (v@location:scope[v.name]), N>;
  else
    return <V + {v@location}, E, N + (v@location:v.name)>;
}

NameGraph resolveNames(call(v, args), map[str,loc] scope) {
  V = {v@location};
  E = (v@location:scope[v.name]);
  N = ();
  for (e <- args) {
    <V2,E2,N2> = resolveNames(e, scope);
    V += V2;
    E += E2;
    N += N2;
  }
  return <V,E,N>;
}

NameGraph resolveNames(block(vars, e), map[str,loc] scope) {
  scope = scope + (v.name:v@location | v <- vars);
  <V,E,N> = resolveNames(e, scope);
  return <V + {v@location | v <- vars}, E, N + (v@location:v.name | v <- vars)>;
}

default NameGraph resolveNames(Exp e, map[str,loc] scope) {
  <V,E,N> = <{},(),()>;
  for (Exp e2 <- e) {
    <V2,E2,N2> = resolveNames(e2, scope);
    <V,E,N> = <V + V2,E + E2, N + N2>;
  }
  return <V,E,N>;
}
  

NameGraph resolveNames(Prog p) {
  topScope = collectDefinitions(p);
  
  <dV,dE,dN> = <{},(),()>;
  for (d <- p.sig) {
    <V2,E2,N2> = resolveNames(d, topScope);
    <dV,dE,dN> = <dV + V2,dE + E2,dN + N2>;
  }
  <mV,mE,mN> = <{},(),()>;
  for (e <- p.main) {
    <V2,E2,N2> = resolveNames(e, topScope);
    <mV,mE,mN> = <mV + V2,mE + E2,mN + N2>;
  }
  
  return <dV + mV,dE + mE, dN + mN>;
}

