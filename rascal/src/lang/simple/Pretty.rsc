module lang::simple::Pretty

import lang::simple::AST;
import lang::simple::Parse;
import lang::simple::Implode;

import List;
import IO;
import String;

str pretty(prog(FDefs fdefs, list[Exp] main)) =
  ("" | it + next | next <- [pretty(fdef) | fdef <- fdefs] + [pretty(exp) | exp <- main]);
  
str pretty(FDef d) =
  "<d.fsym>(<intercalate(", ", [p | p<-d.params])>) =
  '  <pretty(d.body)>;";
  
str pretty(val(nat(n))) = "<n>";
str pretty(val(string(s))) = quoted(s);
str pretty(val(error(s))) = "error(<quoted(s)>)";
str pretty(var(x)) = x;
str pretty(assign(x, e)) = "<x> = <pretty(e)>";
str pretty(vardecl(x, e)) = "var <x> = <pretty(e)>";
str pretty(call(x, es)) = "<x>(<intercalate(", ",[pretty(e)|e<-es])>)";
str pretty(cond(c, t, e)) = "if (<pretty(c)>) then <pretty(t)> else <pretty(e)>";
str pretty(plus(e1,e2)) = "(<pretty(e1)> + <pretty(e2)>)";
str pretty(seq(e1,e2)) = "(<pretty(e1)>; <pretty(e2)>)";
str pretty(eq(e1,e2)) = "(<pretty(e1)> == <pretty(e2)>)";
str pretty(block(e)) = "{ <pretty(e)> }";

str quoted(s) = {
  if (size(s) > 0 && s[0] == "\"")
    return s;
  else
    return "\"<s>\"";
};

//test bool prettyParse(Prog p) = p == implode(parse(pretty(p)));

