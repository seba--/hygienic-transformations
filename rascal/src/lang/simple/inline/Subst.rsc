module lang::simple::inline::Subst

import lang::simple::AST;
import lang::simple::NameRel;
import lang::simple::Pretty;

import Node;
import IO;

import name::Relation;
import name::NameFix;

Prog captureAvoidingSubst(Prog p, str name, Exp e) {
  Gs = resolveNames(p);
  p2 = subst(p, name, e);
  return nameFix(#Prog, Gs, p2, resolveNames);
}

Exp captureAvoidingSubstExp(Exp exp, str name, Exp e) {
  Gs = mainResolveNamesExp(exp);
  exp2 = substExp(exp, name, e);
  return nameFix(#Prog, Gs, exp2, mainResolveNamesExp);
}

Prog subst(Prog p, str name, Exp e) {
  fdefs = [ substFDef(fdef, name, e) | fdef <- p.fdefs ];
  main = [substExp(exp, name, e) | exp <- p.main ];
  return prog(fdefs, main);
}

FDef substFDef(FDef def, str name, Exp e) {
  if (name in def.params)
    return def;
  Exp body = substExp(def.body, name, e);
  return fdef(def.fsym, def.params, body);
}

Exp substExp(var(y), str x, Exp e) = x == y ? e : var(y);
Exp substExp(let(y, e1, e2), str x, Exp e) = let(y, substExp(e1, x, e), x == y ? e2 : substExp(e2, x, e));

// want to use "for (Exp e2 <- e1) insert subE(e2, x, e)" as syntactic sugar
default Exp substExp(Exp e1, str x, Exp e) =
   top-down-break visit(e1) {
    case Exp sub => substExp(sub, x, e) when sub != e1
    //case var(x) => e 
  };



