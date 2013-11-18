module lang::simple::locfun::MendCall


import lang::simple::AST;
import lang::simple::locfun::AST;


VDef mendcallVDef(vdef(str name, Exp exp), str fsym, str fsym1, list[str] fvns) {
  exp1 = mendcall(exp, fsym, fsym1, fvns);
  return vdef(name, exp1);
}


VDef mendcallFDef(fd:fdef(str name, list[str] pnames, Exp body),
                  str fsym, str fsym1, list[str] fvns) {
  if (name == fsym)
    return fd;
  else {
    body1 = mendcall(body, fsym, fsym1, fvns);
    return fdef(fsym, pnames, body1);
  }
}


Exp mendcall(assign(str name, Exp exp), str fsym, str fsym1, list[str] fvns) {
  exp1 = mendcall(exp, fsym, fsym1, fvns);
  return assign(name, exp1); 
}

Exp mendcall(exp:call(str name, list[Exp] opds), str fsym, str fsym1, list[str] fvns) {
  opds1 = [mendcall(opd, fsym, fsym1, fvns) | Exp opd <- opds];
  if (name == fsym)
    return call(fsym1, [var(fvn) | str fvn <- fvns] + opds1);
  else
    return call(name, opds1);
}

Exp mendcall(cond(Exp cexp, Exp texp, Exp fexp), str fsym, str fsym1, list[str] fvns) {
  cexp1 = mendcall(cexp, fsym, fsym1, fvns);
  texp1 = mendcall(texp, fsym, fsym1, fvns);
  fexp1 = mendcall(fexp, fsym, fsym1, fvns);
  return cond(cexp1, texp1, fexp1);
}

Exp mendcall(plus(Exp lhs, Exp rhs), str fsym, str fsym1, list[str] fvns) {
  lhs1 = mendcall(lhs, fsym, fsym1, fvns);
  rhs1 = mendcall(rhs, fsym, fsym1, fvns);
  return plus(lhs1, rhs1);
}

Exp mendcall(sequ(Exp fst, Exp snd), str fsym, str fsym1, list[str] fvns) {
  fst1 = mendcall(fst, fsym, fsym1, fvns);
  snd1 = mendcall(snd, fsym, fsym1, fvns);
  return sequ(fst1, snd1);
}

Exp mendcall(equ(Exp lhs, Exp rhs), str fsym, str fsym1, list[str] fvns) {
  lhs1 = mendcall(lhs, fsym, fsym1, fvns);
  rhs1 = mendcall(rhs, fsym, fsym1, fvns);
  return equ(lhs1, rhs1);
}

Exp mendcall(block(list[VDef] vini, Exp body), str fsym, str fsym1, list[str] fvns) {
  vdef1 = [mendcallVDef(vdef) | VDef vdef <- vini];
  for (vdef(str name, _) <- vini) {
    if (name == fsym)
      return block(vdef1, body);
    else {
      body1 = mendcall(body, fsym, fsym1, fvns);
      return block(vdef1, body1);
    }
  }
}

Exp mendcall(block(list[FDef] fini, Exp body), str fsym, list[str] fvns) {
  fdef1 = [mendcallFDef(fdef) | FDef fdef <- fini];
  for (fdef(str name, _, _) <- fini) {
    if (name == fsym)
      return block(fdef1, body);
    else {
      body1 = mendcall(body, fsym, fsym1, fvns);
      return block(fdef1, body1);
    }
  }
}

default Exp mendcall(Exp exp, str _, str _, list[str] _) = exp;


