module lang::lambda::Syntax

import IO;

// ****************
// concrete syntax
// ****************
lexical Nat = [0-9][0-9]* !>> [0-9];
lexical Id = ([a-zA-Z][a-zA-Z0-9_\-]* !>> [a-zA-Z0-9_\-]) \ Reserved ;
keyword Reserved = "lambda" | "if" | "then" | "else";

start syntax Exp = var: Id v
                 | nat: Nat n
                 | left app: Exp e1 Exp e2
                 > right plus: Exp e1 "+" Exp e2
                 > lambda: "lambda" Id v "." Exp body
                 | bracket "(" Exp ")";

lexical Comment = @category="Comment" "//" ![\n\r]* $;
lexical Whitespace = [\u0009-\u000D \u0020 \u0085 \u00A0 \u1680 \u180E \u2000-\u200A \u2028 \u2029 \u202F \u205F \u3000];
syntax WhitespaceOrComment = whitespace: Whitespace | comment: Comment ; 
layout Standard = WhitespaceOrComment* !>> [\ \t\n\f\r] !>> "//";


// ****************
// abstract syntax
// ****************
data Exp = var(str v)
         | nat(int n)
         | plus(Exp e1, Exp e2)
         | lambda(str v, Exp body)
         | app(Exp e1, Exp e2);

// ****************
// parsing
// ****************
int nextParse = 0;
Exp parse(str src){
  file = |project://Rascal-Hygiene/output/| + "stdin<nextParse>.lambda";
  nextParse = nextParse + 1;
  writeFile(file, src);
  return implode(#Exp, parse(#start[Exp], src, file));
}

// ****************
// printing
// ****************
str print(var(v)) = v;
str print(nat(n)) = "<n>";
str print(plus(e1, e2)) = "(<print(e1)> + <print(e2)>)";
str print(app(e1, e2)) = "(<print(e1)> <print(e2)>)";
str print(lambda(v, body)) = "(lambda <v>. <print(body)>)";
