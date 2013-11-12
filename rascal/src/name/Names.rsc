module name::Names

import String;

alias ID = set[loc];

ID getID(str x) = originsOnly(x);

str setID(str x, ID orgs) = setOrigins(x, orgs);
