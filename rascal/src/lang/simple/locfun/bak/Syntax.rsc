module lang::simple::locfun::Syntax


import lang::simple::Syntax;


syntax Exp = block: "{" FDef? fini Exp body "}";

