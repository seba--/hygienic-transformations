module lang::simple::Syntax

start syntax Prog = prog: FDef* defs Exp? main;

syntax VDef = vdef: "var" Id name Exp exp ";";

syntax FDef = fdef: "fun" Id fsym "(" {Id ","}* pnames ")" "=" Exp body ";";

syntax Exp = val: Val v
           | var: Id x
           | call: Id "(" {Exp ","}* args ")"
           | cond: "if" Exp "then" Exp "else" Exp
           | right plus: Exp "+" Exp 
           > non-assoc equ: Exp "==" Exp
           > assign: Id "=" Exp
           | vardecl: "var" Id "=" Exp
           > right sequ: Exp ";" Exp
           | block: "{" VDef? vini Exp body "}"
           | bracket "(" Exp ")"
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

layout Standard 
  = WhitespaceOrComment* !>> [\ \t\n\f\r] !>> "//";
  
syntax WhitespaceOrComment 
  = whitespace: Whitespace
  | comment: Comment
  ; 
