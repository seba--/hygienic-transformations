module lang::simple::NameRel

import lang::simple::AST;

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

alias Result = rel[str name, loc use, loc def];

Result resolveNames(Def def, map[str,loc] scope) =
  resolveNames(def.body, scope + (p.name:p@location | p <- def.params));

Result resolveNames(evar(v), map[str,loc] scope) = 
  {<v.name, v@location,scope[v.name]>}
  when v.name in scope;
//Result resolveNames(evar(v), map[str,loc] scope) = 
//  {<v.name, UNBOUND>}
//  when v.name notin scope;


Result resolveNames(assign(v, e), map[str,loc] scope) =
  resolveNames(e, scope) + {<v.name, v@location, scope[v.name]>}
  when v.name in scope;

Result resolveNames(assign(v, e), map[str,loc] scope) =
  resolveNames(e, scope + (v.name:v@location))
  when v.name notin scope;

Result resolveNames(call(v, args), map[str,loc] scope) =
  ({} | it + resolveNames(a, scope) | a <- args) + {<v.name, v@location, scope[v.name]>};

Result resolveNames(block(vars, e), map[str,loc] scope) {
  scope = scope + (v.name:v@location | v <- vars);
  return resolveNames(e, scope);
}

default Result resolveNames(Exp e, map[str,loc] scope) =
  ( {} | it + resolveNames(e2, scope) | Exp e2 <- e );
  

Result resolveNames(Prog p) {
  topScope = collectDefinitions(p);
  
  defRels = ({} | it + resolveNames(d, topScope) | d <- p.defs);
  mainRel = ({} | it + resolveNames(e, topScope) | e <- p.main);
  
  return defRels + mainRel;
}

