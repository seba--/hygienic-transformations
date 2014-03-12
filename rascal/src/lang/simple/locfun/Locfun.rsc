module lang::simple::locfun::Locfun

import lang::simple::Syntax;
import lang::simple::AST;
extend lang::simple::NameRel;
extend lang::simple::Pretty;

import name::IDs;
import name::Relation;

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

// desugar
Prog liftLocfun_(prog(fdefs, main), NameGraph ng) {
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

Exp extendCalls(str name, Exp e, list[str] vars, ng ) = visit (e) {
  case call(cname, args) => call(cname, args + [var(v) | v <- vars])
    when isRefOf(cname, name, ng), bprintln("Updating call <cname>")
};
Prog liftLocfunNested(prog(fdefs , main), NameGraph ng) {
  
  list[FDef] new = [];
  
  Exp liftE(Exp e) = top-down-break visit(e) {
      case let(f:fdef(name, params, body), bexp): {
        println("Lifting <name>");
        free = dup([ n | /var(n) := body ] - name - params);
        new += fdef(name, params + free, liftE(extendCalls(name, body, free, ng )));
        insert liftE(extendCalls(name, bexp, free, ng ));
     }
  };
  
  main = [ liftE(m) | m <- main ];
  return prog(fdefs + new, main);
}

// Assumption: local functions do not contain other local functions.
// "liftLocfunNested" above gives an implementation supporting nested local functions. 
Prog liftLocfun(prog(fdefs, main), G) {
  lifted = [];
  liftedMain = outermost visit(main) {
    case let(fdef(f, params, body),e2): {
      free = dup([ n | /var(n) := body ] - f - params);
      lifted += fdef(f, params + free, extendCalls(f, body, free, G));
      insert extendCalls(f, e2, free, G );
    }
  };
  return prog(fdefs + lifted, liftedMain); 
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

