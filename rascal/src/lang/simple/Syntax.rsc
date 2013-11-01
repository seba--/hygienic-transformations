module lang::simple::Syntax

start syntax Prog = prog: {FDef ";"}* defs (";" Exp main)?;

syntax FDef = fdef: Var fsym "(" {Var ","}* params ")" "=" Exp body;

syntax Exp = val: Val v
           | var: Var x
           | call: Var "(" {Exp ","}* args ")"
           | cond: "if" Exp "then" Exp "else" Exp
           | right plus: Exp "+" Exp 
           > non-assoc eq: Exp "==" Exp
           > assign: Var "=" Exp
           > right seq: Exp ";" Exp
           | block: "{" Exp e "}"
           | bracket "(" Exp ")"
           ;

syntax Var = sym: Id;
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
