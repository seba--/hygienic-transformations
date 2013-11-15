module lang::java::NameRel

import name::Relation;
import name::Names;

import lang::java::jdt::m3::Core;

NameGraph m3toNameGraph(M3 m) {
  Edges e = ( {x}: {y} | <x, y> <- m@uses o m@declarations );
  
  map[ID, str] n = ( m@declarations[x]: n | <str n, loc x>  <- m@names );
  
  set[ID] v = { {x} | x <- m@uses<0> + m@declarations<1> };
  
  return <v, e, n>;
  
}

