module lang::missgrant::syntax::MissGrant

start syntax Controller = controller: Events events ResetEvents? resets Commands? commands State+ states;

syntax Events = @Foldable "events" Event* "end";
syntax ResetEvents = @Foldable "resetEvents" Id* "end"; 
syntax Commands = @Foldable "commands" Command* "end";

syntax Command = command: Id name Id token;
syntax Event = event: Id name Id token;

syntax State = @Foldable state: "state" Id name Actions? Transition* "end";
syntax Actions = "actions" "{" Id+ "}";

syntax Transition = transition: Id event "=\>" Id state;

syntax Id = lex [a-zA-Z][a-zA-Z0-9_]* - Reserved # [a-zA-Z0-9_];

syntax Reserved = "events" | "end" | "resetEvents" | "state" | "actions" ;

syntax LAYOUT 
	= lex whitespace: [\t-\n\r\ ] 
    | lex Comment ;

layout LAYOUTLIST 
    = LAYOUT* 
	# [\t-\n\r\ ] 
	# "/*" ;

syntax Comment 
	= lex @category="Comment"  "/*" CommentChar* "*/" ;

syntax CommentChar 
	= lex ![*] | lex Asterisk ;

syntax Asterisk
	= lex [*] # [/] ;
 