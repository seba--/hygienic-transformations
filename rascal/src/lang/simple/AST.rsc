module lang::simple::AST

alias FDefs = list[FDef];

data Prog = prog(FDefs fdefs, list[Exp] main);

data VDef = vdef(str name, Exp exp);

data FDef = fdef(str fsym, list[str] pnames, Exp body);

data Exp = val(Val v)
         | var(str x)
         | assign(str var, Exp e)
         | call(str fsym, list[Exp] args)
         | cond(Exp c, Exp t, Exp e)
         | plus(Exp e1, Exp e2)
         | sequ(Exp e1, Exp e2)
         | equ(Exp e1, Exp e2)
         | block(list[VDef] vdef, Exp body)
         ;

data Val = nat(int n) | string(str s) | error(str msg);
