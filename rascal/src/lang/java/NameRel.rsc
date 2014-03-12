module lang::java::NameRel

import name::Relation;
import name::IDs;

import lang::java::jdt::m3::Core;

NameGraph m3toNameGraph(M3 m) {
  Edges e = ( [x]: [y] | <x, y> <- m@uses o m@declarations );
  
  map[ID, str] n = ( [v]: n | <str n, loc x>  <- m@names, v <- m@declarations[x] );
  
  fewerDecls = ( {} | it + m@declarations[x] | <_, loc x>  <- m@names );
  
   //{ d | d <- m@declarations<1>.
   // TODO: get rid of packages.
  
  set[ID] v = { [x] | x <- m@uses<0> + fewerDecls };
  
  return <v, e>;
  
}

