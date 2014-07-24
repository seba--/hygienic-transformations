module lang::simple::Syntax

start syntax Prog = prog: (FDef ";")* fdefs Exp? main;

syntax FDef = fdef: "fun" Id fsym "(" {Id ","}* params ")" "=" Exp body;

syntax Exp = val: Val v
           | var: Id x
           | call: Id fsym "(" {Exp ","}* args ")"
           | not: "!" Exp e
           | cond: "if" Exp c "then" Exp t "else" Exp e
           | right times: Exp e1 "*" Exp e2 
           > right plus: Exp e1 "+" Exp e2 
           > non-assoc equ: Exp e1 "==" Exp e2
           > left or: Exp e1 "||" Exp e2
           > assign: Id var "=" Exp e
           > right sequ: Exp e1 ";" Exp e2
           | let: "let" Id x "=" Exp e "in" Exp body
           | bracket "(" Exp ")"
           | \catch: "catch" "{" Exp "}"
		           | aif: "aif" Exp c "then" Exp t "else" Exp e
           ;

syntax Val = nat: Nat | string: String | error: "error" "(" String ")";


lexical Nat = [0-9][0-9]* !>> [0-9];

lexical Id = ([a-zA-Z][a-zA-Z0-9_\-]* !>> [a-zA-Z0-9_\-]) \ Reserved ;
lexical String = "\"" ![\"]* "\""; 
keyword Reserved = "error" | "if" | "then" | "else";

lexical Comment = @category="Comment" "//" ![\n\r]* $;
lexical Whitespace 
  = [\u0009-\u000D \u0020 \u0085 \u00A0 \u1680 \u180E \u2000-\u200A \u2028 \u2029 \u202F \u205F \u3000]
  ; 
syntax WhitespaceOrComment 
  = whitespace: Whitespace
  | comment: Comment
  ; 
layout Standard 
  = WhitespaceOrComment* !>> [\ \t\n\f\r] !>> "//";
  
