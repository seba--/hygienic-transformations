module lang::simple::inline::Inline

import List;

import lang::simple::AST;
import lang::simple::NameRel;

import lang::simple::inline::Subst;

import name::NameFix;

Prog captureAvoidingInline(Prog p, str name) {
  if ({def} := { d | d <- p.fdefs, d.fsym == name}) {
    Gs = resolveNames(p);
  	p2 = inline(p, def);
    return nameFix(#Prog, Gs, p2, resolveNames);
 }
}

Prog captureAvoidingInline2(Prog p, str name) {
  if ({def} := { d | d <- p.fdefs, d.fsym == name})
  	return captureAvoidingInline2(p, def);
}

Prog captureAvoidingInline2(Prog p, FDef def) =
  visit(p) {
    case call(f, args): 
      if (f == def.fsym && size(def.params) == size(args))
        insert ( def.body | captureAvoidingSubstExp(it, x, a) | <x,a> <- zip(def.params, args)); 
  };


Prog inline(Prog p, str name) {
  if ({def} := { d | d <- p.fdefs, d.fsym == name})
  	return inline(p, def);
}

Prog inline(Prog p, FDef def) =
  visit(p) {
    case call(f, args): 
      if (f == def.fsym && size(def.params) == size(args))
        insert ( def.body | substExp(it, x, a) | <x,a> <- zip(def.params, args)); 
  };
