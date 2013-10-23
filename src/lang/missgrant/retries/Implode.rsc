module lang::missgrant::retries::Implode

import lang::missgrant::retries::Parse;
import lang::missgrant::retries::AST;

import ParseTree;
import Node;

Controller implode(Tree pt) = implode(#Controller, pt);

Controller load(loc l) = implode(#Controller, parse(l));
