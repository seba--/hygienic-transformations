module lang::simple::AST

alias FDefs = list[FDef];

data Prog = prog(FDefs fdefs, list[Exp] main);

data FDef = fdef(Var fsym, list[Var] params, Exp body);

data VDef = vdef(Var var, Exp exp);

data Exp = val(Val v)
         | var(Var x)
         | assign(Var var, Exp e)
         | call(Var fsym, list[Exp] args)
         | cond(Exp c, Exp t, Exp e)
         | plus(Exp e1, Exp e2)
         | seq(Exp e1, Exp e2)
         | eq(Exp e1, Exp e2)
         | block(list[VDef] vdefs, Exp e)
         ;
data Var = sym(str name);
data Val = nat(int n) | string(str s) | error(str msg);

anno loc Var@location; 

Var name2var(str name) = sym(name);