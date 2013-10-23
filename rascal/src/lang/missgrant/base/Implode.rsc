module lang::missgrant::base::Implode

import lang::missgrant::base::Parse;
import lang::missgrant::base::AST;

import ParseTree;
import Node;

Controller implode(Tree pt) = implode(#Controller, pt);

Controller load(loc l) = implode(#Controller, parse(l));
