module lang::simple::locfun::Locfun

import lang::simple::Syntax;
import lang::simple::AST;
extend lang::simple::NameRel;
extend lang::simple::Pretty;

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
  
  <<dV, dE>, _> = resolveNamesFDef(fdef, lscope);
  lscope = lscope + (fdef.fsym : getID(fdef.fsym));
  
  <<V, E>, _> = resolveNamesExp(body, lscope);
  return <<V + dV, E + dE>, scope>;
}

// desugar
Prog liftLocfun_(prog(fdefs, main), NameGraph ng) {
  lifted = [];
  
  liftedMain = visit(main) {
    case b:block(fdef(name, params, body), bexp): {
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

Prog liftLocfun(prog(fdefs , main), NameGraph ng) {
  Exp substCalls(str name, Exp e, list[str] vars ) = visit (e) {
    case call (cname, args) => call(cname, args + [var(v) | v <- vars])
      when isRefOf(cname, name, ng)
  };
  list[FDef] new = [];
  main = bottom-up visit(main) {
     case block(f:fdef(name, params, body), bexp): {
       free = [ n | /var(n) := body ] - name - params;
       f2 = fdef(name, params + free, body);
       new = [f2, *new];
       new = [fdef(n, ps, substCalls(name, b, free)) | fdef(n, ps, b) <- new ];
       insert substCalls (name, bexp, free );
     }
  };
  return prog(fdefs + new, main);
}

//@doc{Computes set of unbound variables in expression (as list).}
//list[str] freevars(Exp e) {
//  <G,_> = resolveNamesExp(e, ());
//  return [v | /var(v) <- e, isFree(v, G)];
//}
//
//@doc{Computes set of call-sites for given (labeled) definition name (as list).}
//list[str] callsTo(str def, Exp e) {
//  <G,_> = resolveNamesExp(e, ());
//  return [ name | /call(name, _) <- e, isRefOf(name, def, G) ];
//}

