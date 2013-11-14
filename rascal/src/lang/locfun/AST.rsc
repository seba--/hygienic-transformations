module lang::locfun::AST


import lang::simple::AST;


data Exp = block(list[FDef] fini, Exp body);

