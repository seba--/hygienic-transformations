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

NameRel resolveNames(Def def, map[str,loc] scope) =
  resolveNames(def.body, scope + (p.name:p@location | p <- def.params));

NameRel resolveNames(evar(v), map[str,loc] scope) = 
  {<v.name, v@location,scope[v.name]>}
  when v.name in scope;
//NameRel resolveNames(evar(v), map[str,loc] scope) = 
//  {<v.name, UNBOUND>}
//  when v.name notin scope;


NameRel resolveNames(assign(v, e), map[str,loc] scope) =
  resolveNames(e, scope) + {<v.name, v@location, scope[v.name]>}
  when v.name in scope;

NameRel resolveNames(assign(v, e), map[str,loc] scope) =
  resolveNames(e, scope + (v.name:v@location))
  when v.name notin scope;

NameRel resolveNames(call(v, args), map[str,loc] scope) =
  ({} | it + resolveNames(a, scope) | a <- args) + {<v.name, v@location, scope[v.name]>};

NameRel resolveNames(block(vars, e), map[str,loc] scope) {
  scope = scope + (v.name:v@location | v <- vars);
  return resolveNames(e, scope);
}

default NameRel resolveNames(Exp e, map[str,loc] scope) =
  ( {} | it + resolveNames(e2, scope) | Exp e2 <- e );
  

NameRel resolveNames(Prog p) {
  topScope = collectDefinitions(p);
  
  defRels = ({} | it + resolveNames(d, topScope) | d <- p.defs);
  mainRel = ({} | it + resolveNames(e, topScope) | e <- p.main);
  
  return defRels + mainRel;
}

