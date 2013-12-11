module name::Names

import String;
import util::Maybe;

alias ID = list[loc];

ID getID(str x) = [ l | <just(loc l), _> <- origins(x) ];
  //originsOnly(x);

str setID(str x, ID orgs) = setOrigins(x, orgs);
