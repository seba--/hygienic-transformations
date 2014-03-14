module lang::simple::inline::Inline

import List;

import lang::simple::AST;
import lang::simple::NameRel;

import lang::simple::inline::Subst;

import name::NameFix;
import vis::Figure;

Prog captureAvoidingInline(Prog p, list[str] names) {
  defs = [ d | d <- p.fdefs, d.fsym in names ];
  Gs = resolveNames(p);
  p2 = ( p | inline(it, def) | def <- defs );
  return nameFix(#Prog, Gs, p2, resolveNames);
}

Prog captureAvoidingInline(Prog p, str name) = captureAvoidingInline(p, [name]); 


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
