module lang::missgrant::format::ToBox

import lang::missgrant::ast::MissGrant;
import lang::box::util::Box;
import List;


data Controller = controller(list[Event] events, 
					list[str] resets, 
					list[Command] commands,
					list[State] states);

data State = state(str name, list[str] actions, list[Transition] transitions);

data Command = command(str name, str token);
data Event = event(str name, str token);
data Transition = transition(str event, str state);


public Box controller2box(Controller ctl) {

}

public Box events2box(list[Event] es) {

}

public Box commands2box(list[Command] cs) {

}

public Box state2box(State s) {

}


public Box mod2box(Module m) {
  return V([
    H([KW(L("MODULE")), H([id2box(m.name), L(";")])[@hs=0]])[@hs=1],
    decls2box(m.decls),
    KW(L("BEGIN")),
    I([
      V(hsepList(m.body, ";", stat2box))[@vs=0]
    ]),
    H([H([KW(L("END")), id2box(m.endName)])[@hs=1], L(".")])[@hs=0]
  ])[@vs=1];
}

public Box decls2box(decls(consts, types, vars)) = 
	V(consts2boxes(consts) + types2boxes(types) + vars2boxes(vars))[@vs=1];

public list[Box] consts2boxes(list[ConstDecl] consts) {
   	if (consts != []) {
     	return [KW(L("CONST")), I([A([constDecl2box(x) | x <- consts])])];
   	}
   	return [];
}

public list[Box] types2boxes(list[TypeDecl] types) {
   	if (types != []) {
     	return [KW(L("TYPE")), I([A([typeDecl2box(x) | x <- types])])];
   	}
   	return [];
}

public list[Box] vars2boxes(list[VarDecl] vars) {
   	if (vars != []) {     
		return [KW(L("VAR")), I([A([varDecl2box(x) | x <- vars])[@vs=1]])];
   	}
   	return [];
}


public Box constDecl2box(ConstDecl cd) {
 	return R([L(cd.name.name), L("="), H([exp2box(cd.\value), L(";")])[@hs=0]]);
}

public Box typeDecl2box(TypeDecl td) {
  	return R([L(td.name.name), L(":"), H([type2box(td.\type), L(";")])[@hs=0] ]);
}

public Box varDecl2box(VarDecl vd) {
  	return R([H(hsepList(vd.names, ",", id2box))[@hs=1], L(":"), H([type2box(vd.\type), L(";")])[@hs=0]]);
}

public Box stat2box(assign(Ident var, Expression exp)) = H([id2box(var), L(":="), exp2box(exp)])[@hs=1];

public Box stat2box(ifThen(Expression condition, list[Statement] body, list[tuple[Expression condition, list[Statement] body]] elseIfs, list[Statement] elsePart)) {
    ift = V([
        H([L("IF"), exp2box(condition), L("THEN")])[@hs=1],
          I([V(hsepList(body, ";", stat2box))])]);
    elifs = for (<cond, ebody> <- elseIfs) {
      append V([
        H([L("ELSIF"), exp2box(cond), L("THEN")])[@hs=1],
        I(hsepList(ebody, ";", stat2box))
      ]);
    }
    Box els;
    if(elsePart != []){
    	els = V([
        	L("ELSE"),
        	I(hsepList(elsePart, ";", stat2box))
    	]);
    } else {
    	els = [];
    }
    return V([ift] + elifs + [els] + [KW(L("END"))]);
    
}
    
public Box stat2box(whileDo(Expression condition, list[Statement] body)) = V([
            H([L("WHILE"), exp2box(condition), L("DO")])[@hs=1],
             I([V(hsepList(body, ";", stat2box))]),
            KW(L("END"))
        ]);   


public Box exp2box(nat(int val)) = L("<val>");
public Box exp2box(\true()) = L("TRUE");
public Box exp2box(\false()) = L("FALSE");
public Box exp2box(lookup(Ident var)) = id2box(var);
public Box exp2box(neg(Expression arg)) = H([L("("),L("-"), exp2box(arg),L(")")])[@hs=0];
public Box exp2box(pos(Expression arg)) = H([L("("),L("+"), exp2box(arg),L(")")])[@hs=0];
public Box exp2box(not(Expression arg)) = H([L("("),L("~"), exp2box(arg),L(")")])[@hs=0];
public Box exp2box(mul(Expression lhs, Expression rhs)) = H([L("("), exp2box(lhs), L("*"), exp2box(rhs),L(")")])[@hs=1];
public Box exp2box(div(Expression lhs, Expression rhs)) = H([L("("),exp2box(lhs), L("DIV"), exp2box(rhs),L(")")])[@hs=1];
public Box exp2box(Expression::mod(Expression lhs, Expression rhs)) = H([L("("),exp2box(lhs), L("MOD"), exp2box(rhs),L(")")])[@hs=1];
public Box exp2box(amp(Expression lhs, Expression rhs)) = H([L("("),exp2box(lhs), L("&"), exp2box(rhs),L(")")])[@hs=1];
public Box exp2box(add(Expression lhs, Expression rhs)) = H([L("("),exp2box(lhs), L("+"), exp2box(rhs),L(")")])[@hs=1];
public Box exp2box(sub(Expression lhs, Expression rhs)) = H([L("("),exp2box(lhs), L("-"), exp2box(rhs),L(")")])[@hs=1];
public Box exp2box(or(Expression lhs, Expression rhs)) = H([L("("),exp2box(lhs), L("OR"), exp2box(rhs),L(")")])[@hs=1];
public Box exp2box(eq(Expression lhs, Expression rhs)) = H([L("("),exp2box(lhs), L("="), exp2box(rhs),L(")")])[@hs=1];
public Box exp2box(neq(Expression lhs, Expression rhs)) = H([L("("),exp2box(lhs), L("#"), exp2box(rhs),L(")")])[@hs=1];
public Box exp2box(lt(Expression lhs, Expression rhs)) = H([L("("),exp2box(lhs), L("\<"), exp2box(rhs),L(")")])[@hs=1];
public Box exp2box(gt(Expression lhs, Expression rhs)) = H([L("("),exp2box(lhs), L("\>"), exp2box(rhs),L(")")])[@hs=1];
public Box exp2box(leq(Expression lhs, Expression rhs)) = H([L("("),exp2box(lhs), L("\<="), exp2box(rhs),L(")")])[@hs=1];
public Box exp2box(geq(Expression lhs, Expression rhs)) = H([L("("),exp2box(lhs), L("\>="), exp2box(rhs),L(")")])[@hs=1];



public Box type2box(user(Ident name)) = id2box(name);

public list[Box] hsepList(list[&T] elts, str sep, Box(&T) tobox) {
	if (elts == []) { 
     	return [];
    }
  	result = [];
  	Box lst = tobox(head(elts));
  	for (e <- tail(elts)) {
   		result += [H([lst, L(sep)])[@hs=0]];
   		lst = tobox(e);
  	}
  	return result + [lst];
}


public Box id2box(Ident id) {
	return VAR(L(id.name));
}


