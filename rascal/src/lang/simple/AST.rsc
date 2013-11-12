module lang::simple::AST

alias FDefs = list[FDef];

data Prog = prog(FDefs fdefs, list[Exp] main);

data FDef = fdef(str fsym, list[str] params, Exp body);

data Exp = val(Val v)
         | var(str x)
         | assign(str var, Exp e)
         | vardecl(str var, Exp e)
         | call(str fsym, list[Exp] args)
         | cond(Exp c, Exp t, Exp e)
         | plus(Exp e1, Exp e2)
         | seq(Exp e1, Exp e2)
         | eq(Exp e1, Exp e2)
         | block(Exp e)
         ;

data Val = nat(int n) | string(str s) | error(str msg);
