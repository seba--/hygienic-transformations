module lang::missgrant::syntax::MissGrant

// TODO: need bits for final/initial states.

start syntax Controller = controller: Events ResetEvents? Commands? State+;
syntax Events = "events" Event* "end";
syntax ResetEvents = "resetEvents" Id* "end"; 
syntax Commands = "commands" Command* "end";

syntax Command = command: Id Id;
syntax Event = event: Id Id;

syntax State = state: "state" Id Actions? Transition+ "end";
syntax Actions = "actions" "{" Id+ "}";

syntax Transition = transition: Id "=\>" Id;

syntax Id = lex [a-zA-Z][a-zA-Z0-9_]* - Reserved # [a-zA-Z0-9_];

syntax Reserved = "events" | "end" | "resetEvents" | "state" | "actions" ;

syntax LAYOUT 
	= lex whitespace: [\t-\n\r\ ] 
    | lex Comment ;

layout LAYOUTLIST 
    = LAYOUT* 
	# [\t-\n \r \ ] 
	# "/*" ;

syntax Comment 
	= lex @category="Comment"  "/*" CommentChar* "*/" ;

syntax CommentChar 
	= lex ![*] | lex Asterisk ;

syntax Asterisk
	= lex [*] # [/] ;
 