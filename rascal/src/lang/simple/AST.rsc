module lang::simple::AST

alias Sig = list[Def];

data Prog = prog(Sig sig, list[Exp] main)
          ;
data Def = define(str name, list[str] params, Exp body)
         //| var(str name, Exp e)
         ;
data Exp = val(Val v)
         | evar(str x)
         | assign(str name, Exp e)
         | call(str name, list[Exp] args)
         | cond(Exp c, Exp t, Exp e)
         | plus(Exp e1, Exp e2)
         | seq(Exp e1, Exp e2)
         | eq(Exp e1, Exp e2)
         | block(list[str] locals, Exp e)
         ;
data Val = nat(int n) | string(str s) | error(str name);

