module lang::simple::locfun::Abstract


import Prelude;
import lang::simple::AST;
import lang::simple::locfun::AST;
import lang::simple::locfun::Decorate;
import lang::simple::locfun::MendCall;


anno list[FDef] VDef @ lfs;
anno list[FDef] FDef @ lfs;
anno list[FDef] Exp @ lfs;


VDef abstractVDef(vdef(str name, Exp exp)) {
  exp1 = abstractExp(exp);
  vdef1 = vdef(name, exp1);
  return vdef1 @ lfs = exp1 @ lfs;
}


FDef abstractFDef(fd:fdef(str fsym, list[str] pnames, Exp body)) {
  body1 = abstractExp(body);
  fdef1 = fdef(fsym, pnames, body1);
  return fdef1 @ lfs = body1 @ lfs;
}


Exp abstractExp(assign(str name, Exp exp)) {
  exp1 = abstractExp(exp);
  ass1 = assign(name, exp1);
  return ass1 @ lfs = exp1 @ lfs;
}

Exp abstractExp(call(str fsym, list[Exp] opds)) {
  opds1 = [abstractExp(opd) | opd <- opds];
  call1 = call(fsym, opds1);
  return call1 @ lfs = ([] | it + opd @ lfs | Exp opd <- opds1);
}

Exp abstractExp(cond(Exp cexp, Exp texp, Exp fexp)) {
  cexp1 = abstractExp(cexp);
  texp1 = abstractExp(texp);
  fexp1 = abstractExp(fexp);
  cond1 = cond(cexp1, texp1, fexp1);
  return cond1 @ lfs = cexp1 @ lfs + texp1 @ lfs + fexp1 @ lfs;
}

Exp abstractExp(plus(Exp lhs, Exp rhs)) {
  lhs1 = abstractExp(lhs);
  rhs1 = abstractExp(rhs);
  plus1 = plus(lhs1, rhs1);
  return plus1 @ lfs = lhs1 @ lfs + rhs1 @ lfs;
}

Exp abstractExp(sequ(Exp fst, Exp snd)) {
  fst1 = abstractExp(fst);
  snd1 = abstractExp(snd);
  sequ1 = seqc(fst1, snd1);
  return sequ1 @ lfs = fst1 @ lfs + snd1 @ lfs;
}

Exp abstractExp(equ(Exp lhs, Exp rhs)) {
  lhs1 = abstractExp(lhs);
  rhs1 = abstractExp(rhs);
  equ1 = equ(lhs1, rhs1);
  return equ1 @ lfs = lhs1 @ lfs + rhs1 @ lfs;
}

Exp abstractExp(block(list[VDef] vini, Exp body)) {
  vini1 = [abstractVDef(vdef) | VDef vdef <- vini];
  body1 = abstractExp(body);
  block1 = block(vini1, body1);
  return block1 @ lfs = (body1 @ lfs | vdef @ lfs + it | VDef vdef <- vini1);
}

Exp abstractExp(block(list[FDef] fini, Exp body)) {
  switch (fini) {
    case [fd:fdef(fsym, pnames, fbod)]: {
      fns = fd @ fns;
      fvns = toList(fns);
      fsym1 = fsym + "_lifted";
      fbod1 = mendcall(fbod, fsym, fsym1, fvns);
      fdef1 = fdef(fsym + "_lifted", fvns + pnames, fbod1);
      fdef1 @ fns = {};
      fdef2 = abstractFDef(fdef1);
      body1 = mendcall(body, fsym, fsym1, fvns);
      body2 = abstractExp(body1);
      block1 = block([], body2);
      return block1 @ lfs = fdef2 @ lfs + [fdef2] + body2 @ lfs;
    }
    default: {
      body1 = abstractExp(body);
      block1 = block([], body1);
      return block1 @ lfs = body1 @ lfs;
    }
  }
}

default Exp abstractExp(Exp exp) {
  return exp @ lfs = [];
}

