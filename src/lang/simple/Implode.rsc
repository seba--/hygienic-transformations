module lang::simple::Implode

import lang::simple::Parse;
import lang::simple::AST;

import ParseTree;
import Node;

Prog implode(Tree pt) = implode(#Prog, pt);
Exp implodeExp(Tree pt) = implode(#Exp, pt);

Prog load(loc l) = implode(#Prog, parse(l));
