module lang::simple::AST

alias Sig = list[Def];

data Prog = prog(Sig sig, list[Exp] main)
          ;
data Def = fdef(Var name, list[Var] params, Exp body)
         //| var(str name, Exp e)
         ;
data Exp = val(Val v)
         | evar(Var x)
         | assign(Var name, Exp e)
         | call(Var name, list[Exp] args)
         | cond(Exp c, Exp t, Exp e)
         | plus(Exp e1, Exp e2)
         | seq(Exp e1, Exp e2)
         | eq(Exp e1, Exp e2)
         | block(list[Var] locals, Exp e)
         ;
data Var = var(str name);
data Val = nat(int n) | string(str s) | error(str name);

anno loc Var@location; 

Var name2var(str name) = var(name);