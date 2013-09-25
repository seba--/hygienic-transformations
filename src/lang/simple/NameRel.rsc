module lang::simple::NameRel

import lang::simple::AST;
import name::Relation;

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

map[str,loc] collectDefinitions(Prog p) =
  ( def.name.name:def.name@location | /Def def := p );

NameGraph resolveNames(Def def, map[str,loc] scope) {
  <V, E> = resolveNames(def.body, scope + (p.name:p@location | p <- def.params));
  return <V + <def.name.name,def.name@location> + {<p.name,p@location> | p <- def.params}, E>;
}

NameGraph resolveNames(evar(v), map[str,loc] scope) = 
  <{<v.name,v@location>}, {<v@location,scope[v.name]>}>
  when v.name in scope;
//NameRel resolveNames(evar(v), map[str,loc] scope) = 
//  {<v.name, UNBOUND>}
//  when v.name notin scope;


NameGraph resolveNames(assign(v, e), map[str,loc] scope) =
  <{<v.name,v@location>}, resolveNames(e, scope) + {<v@location, scope[v.name]>}>
  when v.name in scope;

NameGraph resolveNames(assign(v, e), map[str,loc] scope) =
  <{<v.name,v@location>}, resolveNames(e, scope + (v.name:v@location))>
  when v.name notin scope;

NameGraph resolveNames(call(v, args), map[str,loc] scope) {
  <V,E> = <{<v.name,v@location>}, {<v@location, scope[v.name]>}>;
  for (a <- args) {
    <V2,E2> = resolveNames(a, scope);
    <V,E> = <V + V2,E + E2>;
  }
  return <V,E>;
}

NameGraph resolveNames(block(vars, e), map[str,loc] scope) {
  scope = scope + (v.name:v@location | v <- vars);
  <V,E> = resolveNames(e, scope);
  return <V + {<v.name,v@location> | v <- vars}, E>;
}

default NameGraph resolveNames(Exp e, map[str,loc] scope) {
  <V,E> = <{},{}>;
  for (Exp e2 <- e) {
    <V2,E2> = resolveNames(e2, scope);
    <V,E> = <V + V2,E + E2>;
  }
  return <V,E>;
}
  

NameGraph resolveNames(Prog p) {
  topScope = collectDefinitions(p);
  
  <dV,dE> = <{},{}>;
  for (d <- p.defs) {
    <V2,E2> = resolveNames(d, topScope);
    <dV,dE> = <dV + V2,dE + E2>;
  }
  <mV,mE> = <{},{}>;
  for (e <- p.main) {
    <V2,E2> = resolveNames(e, topScope);
    <mV,mE> = <mV + V2,mE + E2>;
  }
  
  return <dV + mV,dE + mE>;
}

