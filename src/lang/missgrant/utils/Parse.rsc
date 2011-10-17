module lang::missgrant::utils::Parse

import lang::missgrant::syntax::MissGrant;
import ParseTree;

public Controller parse(str src, loc origin) = parse(#Controller, src, origin);

public Controller parse(loc file) = parse(#Controller, file);

