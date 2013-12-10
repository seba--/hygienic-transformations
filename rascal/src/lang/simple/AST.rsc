module lang::simple::AST

alias FDefs = list[FDef];

data Prog = prog(list[FDef] fdefs, list[Exp] main);

data FDef = fdef(str fsym, list[str] params, Exp body);

data Exp = val(Val v)
         | var(str x)
         | not(Exp e)
         | call(str fsym, list[Exp] args)
         | cond(Exp c, Exp t, Exp e)
         | times(Exp e1, Exp e2)
         | plus(Exp e1, Exp e2)
         | equ(Exp e1, Exp e2)
         | sequ(Exp e1, Exp e2)
         | assign(str var, Exp e)
         | let(str x, Exp e, Exp body)
         | \catch(Exp e)
 		 | aif(Exp c, Exp t, Exp e)
         ;

data Val = nat(int n) | string(str s) | error(str msg);
