module lang::locfun::Decorate


import Prelude;
import lang::simple::AST;
import lang::locfun::AST;


anno set[str] VDef @ fns;
anno set[str] FDef @ fns;
anno set[str] Exp @ fns;


VDef decorateVDef(vdef(str name, Exp exp)) {
  exp1 = decorateExp(exp);
  vdef1 = vdef(name, exp1);
  return vdef1 @ fns = exp1 @ fns;
}


FDef decorateFDef(fdef(str fsym, list[str] pnames, Exp body)) {
  body1 = decorateExp(body);
  fdef1 = fdef(fsym, pnames, body1);
  return fdef1 @ fns = body1 @ fns - toSet(pnames) - {fsym};  // self-reference allowed
}


Exp decorateExp(exp:var(str name)) {
  return exp @ fns = {name};
}

Exp decorateExp(assign(str name, Exp exp)) {
  exp1 = decorateExp(exp);
  ass1 = assign(name, exp1);
  return ass1 @ fns = exp1 @ fns;
}

Exp decorateExp(call(str fsym, list[Exp] opds)) {
  opds1 = [decorateExp(opd) | Exp opd <- opds];
  call1 = call(fsym, opds1);
  return call1 @ fns = ({} | it + opd @ fns | Exp opd <- opds1);
}

Exp decorateExp(cond(Exp cexp, Exp texp, Exp fexp)) {
  cexp1 = decorateExp(cexp);
  texp1 = decorateExp(texp);
  fexp1 = decorateExp(fexp);
  cond1 = cond(cexp1, texp1, fexp1);
  return cond1 @ fns = cexp1 @ fns + texp1 @ fns + fexp1 @ fns;
}

Exp decorateExp(plus(Exp lhs, Exp rhs)) {
  lhs1 = decorateExp(lhs);
  rhs1 = decorateExp(rhs);
  plus1 = plus(lhs1, rhs1);
  return plus1 @ fns = lhs1 @ fns + rhs1 @ fns;
}

Exp decorateExp(sequ(Exp fst, Exp snd)) {
  fst1 = decorateExp(fst);
  snd1 = decorateExp(snd);
  sequ1 = sequ(fst1, snd1);
  return sequ1 @ fns = fst1 @ fns + snd1 @ fns;
}

Exp decorateExp(equ(Exp lhs, Exp rhs)) {
  lhs1 = decorateExp(lhs);
  rhs1 = decorateExp(rhs);
  equ1 = equ(lhs1, rhs1);
  return equ1 @ fns = lhs1 @ fns + rhs1 @ fns;
}

Exp decorateExp(block(list[VDef] vini, Exp body)) {
  vini1 = [decorateVDef(vdef) | VDef vdef <- vini];
  body1 = decorateExp(body);
  block1 = block(vini1, body1);
  return block1 @ fns = (body1 @ fns | vdef @ fns + it - {vdef.name} | VDef vdef <- vini1);
}

Exp decorateExp(block(list[FDef] fini, Exp body)) {
  fini1 = [decorateFDef(fdef) | FDef fdef <- fini];
  body1 = decorateExp(body);
  block1 = block(fini1, body1);
  fns1 = (body1 @ fns | fdef @ fns + it - {fdef.fsym} | FDef fdef <- fini1);
  return block1 @ fns = fns1;
}

default Exp decorateExp(Exp exp) {
  return exp @ fns = {};
}

