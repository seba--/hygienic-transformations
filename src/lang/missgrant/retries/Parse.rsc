module lang::missgrant::retries::Parse

import lang::missgrant::retries::Syntax;
import ParseTree;

start[Controller] parse(str src, loc origin) = parse(#start[Controller], src, origin);

start[Controller] parse(loc file) = parse(#start[Controller], file);

