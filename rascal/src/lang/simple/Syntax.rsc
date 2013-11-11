module lang::simple::Syntax

start syntax Prog = prog: Def* defs Exp? main;

syntax Def = define: "define" Id name "(" {Id ","}* params ")" "=" Exp body ";"
           ;

syntax Exp = val: Val v
           | evar: Id x
           | assign: Id "=" Exp
           | call: Id "(" {Exp ","}* args ")"
           | cond: "if" Exp "then" Exp "else" Exp "end"
           | right plus: Exp "+" Exp 
           > non-assoc eq: Exp "==" Exp
           > right seq: Exp ";" Exp
           | block: "{" {Id ","}+ locals ":" Exp e "}"
           | bracket "(" Exp ")"
           ;

syntax Val = nat: Nat | string: String | error: "error" "(" String ")";


lexical Nat = [0-9][0-9]* !>> [0-9];

lexical Id = ([a-zA-Z][a-zA-Z0-9_\-]* !>> [a-zA-Z0-9_\-]) \ Reserved ;
lexical String = "\"" ![\"]* "\""; 
keyword Reserved = "define" | "var" | "error" | "if";

lexical Comment = @category="Comment" "//" ![\n\r]* $;

lexical Whitespace 
  = [\u0009-\u000D \u0020 \u0085 \u00A0 \u1680 \u180E \u2000-\u200A \u2028 \u2029 \u202F \u205F \u3000]
  ; 

layout Standard 
  = WhitespaceOrComment* !>> [\ \t\n\f\r] !>> "//";
  
syntax WhitespaceOrComment 
  = whitespace: Whitespace
  | comment: Comment
  ; 
