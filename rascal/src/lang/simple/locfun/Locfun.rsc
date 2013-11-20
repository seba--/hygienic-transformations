module lang::simple::locfun::Locfun

import lang::simple::Syntax;
import lang::simple::AST;
extend lang::simple::NameRel;
import lang::simple::Pretty;

import name::Names;
import name::Relation;

import IO;
import Set;

// concrete
syntax Exp = block: "{" FDef fdef Exp body "}";

// abstract
data Exp = block(FDef fdef, Exp body);

// pretty
str pretty(block(FDef fdef, Exp body)) = "{ <pretty(fdef)> <pretty(body)> }";

// name resolution
Answer resolveNamesExp(block(FDef fdef, Exp body), Scope scope) {
  lscope = scope;
  
  <<dV, dE, dN>, _> = resolveNamesFDef(fdef, lscope);
  lscope = lscope + (fdef.fsym : getID(fdef.fsym));
  
  <<V, E, N>, _> = resolveNamesExp(body, lscope);
  return <<V + dV, E + dE, N + dN>, scope>;
}

// desugar
Prog liftLocfun(prog(fdefs, main)) {
  lifted = [];
  
  liftedMain = visit(main) {
    case block(fdef(name, params, body), bexp): {
      fvs = freevars(body) - name - params;
      calls = callsTo(name, block(fdef(name, params, body), bexp));
      
      // replace recursive calls to pass along additional arguments
      replaceBody = visit(body) {
        case call(cname, args) =>
             call(cname, args + [var(v) | v <- fvs])
           when cname in calls     
      };
      lifted += fdef(name, params + fvs, replaceBody);
      
      // replace original expression to pass along additional arguments
      replaceExp = visit(bexp) {
        case call(cname, args) =>
             call(cname, args + [var(v) | v <- fvs])
           when cname in calls     
      };
      insert replaceExp;
    }
  };

  return prog(fdefs + lifted, liftedMain);
}

@doc{Computes set of unbound variables in expression (as list).}
list[str] freevars(Exp e) {
  <G,_> = resolveNamesExp(e, ());
  return toList({v | /var(v) <- e, isFree(v, G)});
}

@doc{Computes set of call-sites for given (labeled) definition name (as list).}
list[str] callsTo(str def, Exp e) {
  <G,_> = resolveNamesExp(e, ());
  return toList({name | /call(name, _) <- e, isRefOf(name, def, G)});
}

