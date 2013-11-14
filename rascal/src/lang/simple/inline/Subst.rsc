module lang::simple::inline::Subst

import lang::simple::AST;

import IO;

// import name::Rename;

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

Exp substExp(Exp exp, str name, Exp e) = 
  top-down-break visit(exp) {
    case seq(vardecl(name, e1), e2):
      insert seq(vardecl(name, substExp(e1, name, e)), e2);
    case var(x):
      insert substVar(var(x), name, e);
  };

Exp substVar(var(x), str name, Exp e) = e when x == name;
Exp substVar(var(x), str name, Exp e) = var(x) when x != name;
