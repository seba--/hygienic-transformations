module lang::locfun::Syntax


import lang::simple::Syntax;


syntax Exp = block: "{" FDef? fini Exp body "}";

