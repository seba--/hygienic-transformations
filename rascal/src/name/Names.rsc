module name::Names

import String;

alias ID = set[loc];

ID getID(str x) = originsOnly(x);

str setID(str x, loc l) = setOrigin(x, l);

