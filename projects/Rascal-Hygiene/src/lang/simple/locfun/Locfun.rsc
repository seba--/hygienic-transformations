module lang::simple::locfun::Locfun

extend lang::simple::Syntax;
extend lang::simple::AST;
extend lang::simple::NameRel;
extend lang::simple::Pretty;
extend lang::simple::Implode;

import name::IDs;
import name::NameGraph;

import IO;
import Set;
import List;

// concrete
syntax Exp = let: "let" FDef fdef "in" Exp body;

// abstract
data Exp = let(FDef fdef, Exp body);

// pretty
str pretty(let(FDef fdef, Exp body)) = "let <pretty(fdef)> in <pretty(body)> ";

// name resolution
Answer resolveNamesExp(let(FDef fdef, Exp body), Scope scope) {
  lscope = scope;
  
  lscope = lscope + (fdef.fsym : getID(fdef.fsym));
  <<dV, dE>, _> = resolveNamesFDef(fdef, lscope);
  
  <<V, E>, _> = resolveNamesExp(body, lscope);
  return <<V + dV, E + dE>, scope>;
}

Prog liftLocfun(prog(fdefs, main), NameGraph ng) {
  lifted = [];
  
  liftedMain = visit(main) {
    case b:let(fdef(name, params, body), bexp): {
      fvs = [ n | /var(n) := body ] - name - params;
      
      // replace recursive calls to pass along additional arguments
      replaceBody = visit(body) {
        case call(cname, args) =>
             call(cname, args + [var(v) | v <- fvs])
           when isRefOf(cname, name, ng)     
      };
      lifted += fdef(name, params + fvs, replaceBody);
      
      // replace original expression to pass along additional arguments
      replaceExp = visit(bexp) {
        case call(cname, args) =>
             call(cname, args + [var(v) | v <- fvs])
           when isRefOf(cname, name, ng)     
      };
      insert replaceExp;
    }
  };

  return prog(fdefs + lifted, liftedMain);
}
