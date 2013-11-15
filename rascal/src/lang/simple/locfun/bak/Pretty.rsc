module lang::simple::locfun::Pretty


import lang::simple::Pretty;


str pretty(block(list[FDef] fini, Exp body)) {
  switch (fini) {
    case [FDef fdef]: return "{ <pretty(fdef)> <pretty(body)> }";
    default: return "{ <pretty(body)> }";
  }
}

