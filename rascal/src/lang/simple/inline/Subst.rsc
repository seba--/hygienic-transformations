module lang::simple::inline::Subst

import lang::simple::AST;
import lang::simple::NameRel;
import lang::simple::Pretty;

import IO;

import name::Relation;
import name::NameFix;

Prog captureAvoidingSubst(Prog p, str name, Exp e) {
  Gs = resolveNames(p);
  p2 = paperSubst(p, name, e);
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

Exp substExp(Exp exp, str name, Exp e) = 
  top-down-break visit(exp) {
    case let(x, e2, body) =>
      let(x, substExp(e2, name, e), x == name ? body : substExp(body, name, e))
    case var(x) =>
      substVar(var(x), name, e)
  };

Exp substVar(var(x), str name, Exp e) = e when x == name;
Exp substVar(var(x), str name, Exp e) = var(x) when x != name;


Prog mysubst2(Prog p, str name, Exp e) {
  return visit (p) { 
    case var(name) => e
  }
}

Prog mysubst(Prog p, str name, Exp e) {
  Exp subst(Exp subj) {
    return top-down-break visit(subj) {
      case let(x, e2, body) =>
        let(x, subst(e2), x == name ? body : subst(body))  
      case var(name) =>  e
    }
  }
  fdefs = [ fdef(fn, ps, name in ps ? b : subst(b)) 
            | fdef(fn, ps, b) <- p.fdefs ];
  main = [ subst(exp) | Exp exp <- p.main ];
  return prog(fdefs, main);
}

// only works for single vardefs in blocks.
Prog paperSubst(Prog p, str name, Exp e) {
  Exp subst(Exp subj) = top-down-break visit(subj) {
      case let(n, e2, body) 
         => let(n, subst(e2), n == name ? body : subst(body))
      case var(name) =>  e
  };
  fdefs = [ fdef(fn, ps, name in ps ? b : subst(b)) | fdef(fn, ps, b) <- p.fdefs ];
  return prog(fdefs, [ subst(exp) | Exp exp <- p.main ]);
}


