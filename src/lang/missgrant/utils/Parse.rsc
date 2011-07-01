module lang::missgrant::utils::Parse

import lang::missgrant::syntax::MissGrant;
import ParseTree;

public Controller parse(str src, loc origin) {
  return parse(#Controller, src, origin);
}

public Controller parse(loc file) {
  return parse(#Controller, file);
}

