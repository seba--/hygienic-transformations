module lang::missgrant::retries::Outline

import lang::missgrant::retries::AST;
import ParseTree;
extend lang::missgrant::base::Outline;

node outline(t:transition(e, n, s)) = "transition"()[@label="<e> -\> <s> after <n>"][@\loc=t@location];
