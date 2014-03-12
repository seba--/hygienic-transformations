module name::IDs

import String;
import util::Maybe;

alias ID = list[loc];

ID getID(str x) = [ l | <just(loc l), _> <- origins(x) ];
str setID(str x, ID orgs) = setOrigins(x, orgs);
