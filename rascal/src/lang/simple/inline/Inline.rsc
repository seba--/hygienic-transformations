module lang::simple::inline::Inline

import List;

import lang::simple::AST;

import lang::simple::inline::Subst;

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
