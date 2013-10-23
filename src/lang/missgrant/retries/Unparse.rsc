module lang::missgrant::retries::Unparse

extend lang::missgrant::base::Unparse;
import lang::missgrant::retries::AST;

str unparse(transition(e, t, n, t2)) = "<e> after <n> =\> <t2>";
