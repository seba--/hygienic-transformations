module name::Unbound

str UNBOUND = "UNBOUND";
loc UNBOUND_loc = |rascall://name::Unbound|;

map[loc,loc] mkUnbound(set[loc] ls) = (l:UNBOUND_loc | l <- ls);