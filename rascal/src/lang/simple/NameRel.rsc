module lang::simple::NameRel

import lang::simple::AST;
import name::Relation;
import name::Names;

import IO; 

//anno int Def@location;
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


map[str,ID] collectDefinitions(Prog p) =
  ( def.name: getID(def.name) | /Def def := p );

NameGraph resolveNames(Def def, map[str,ID] scope) {
  <V, E, N> = resolveNames(def.body, scope + (p: getID(p) | p <- def.params));
  return <V + {getID(def.name)} + {getID(p) | p <- def.params}, 
          E,
          N + (getID(def.name): def.name) + (getID(p) :p | p <- def.params)>;
}

NameGraph resolveNames(evar(v), map[str,ID] scope) = 
  <{getID(v)}, (getID(v): scope[v]), ()>
  when v in scope;

NameGraph resolveNames(assign(v, e), map[str,ID] scope) {
  if (v in scope)
    scope2 = scope;
  else
    scope2 = scope + (v: getID(v));
  
  <V,E,N> = resolveNames(e, scope2);
  
  if (v in scope)
    return <V + {getID(v)}, E + (getID(v): scope[v]), N>;
  else
    return <V + {getID(v)}, E, N + (getID(v): v)>;
}

NameGraph resolveNames(call(v, args), map[str,ID] scope) {
  V = {getID(v)};
  E = (getID(v): scope[v]);
  N = ();
  for (e <- args) {
    <V2,E2,N2> = resolveNames(e, scope);
    V += V2;
    E += E2;
    N += N2;
  }
  return <V,E,N>;
}

NameGraph resolveNames(block(vars, e), map[str,ID] scope) {
  scope = scope + (v: getID(v) | v <- vars);
  <V,E,N> = resolveNames(e, scope);
  return <V + {getID(v) | v <- vars}, E, N + (getID(v): v | v <- vars)>;
}

default NameGraph resolveNames(Exp e, map[str,ID] scope) {
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

