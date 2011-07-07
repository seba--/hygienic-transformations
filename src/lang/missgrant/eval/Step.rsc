module lang::missgrant::eval::Step

import lang::missgrant::ast::MissGrant;
import lang::missgrant::extract::ToRelation;
import IO;

alias Output = tuple[str state, list[str] commands];

public tuple[str,list[str]] addCommands(tuple[str,list[str]] a, tuple[str,list[str]] b){
	return <b[0],a[1] + b[1]>;
}

public Output eval(StateTrans trans, Commands commands, str init, list[str] tokens) {
  return (<init,[]> | addCommands(step(trans,commands,it[0],token)) | token <- tokens);
}

public Output step(StateTrans trans, Commands commands, str init,str token){
	if(c <- trans[init,token]){
		return <c,toList(commands[c])>;
	} else {
		return <c,[]>;
	}
}