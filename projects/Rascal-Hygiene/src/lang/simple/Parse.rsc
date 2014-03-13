module lang::simple::Parse

import lang::simple::Syntax;
import ParseTree;

start[Prog] parse(str src, loc origin) = parse(#start[Prog], src, origin);

start[Prog] parse(loc file) = parse(#start[Prog], file);

Exp parseExp(str src) = parse(#Exp, src, |file:///|);

Prog parseProg(str src) = parse(#Prog, src, |file:///|);