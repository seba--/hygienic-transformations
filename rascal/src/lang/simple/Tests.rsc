module lang::simple::Tests


import lang::simple::Syntax;
import lang::simple::Parse;
import lang::simple::AST;
import lang::simple::Implode;
import lang::simple::Eval;


Result eval(Exp exp) = eval([], exp, ());

Result eval(str inp) = eval([], implodeExp(parseExp(inp)), ());


test bool test01() = eval(val(nat(1))).val == nat(1);

test bool test01() = eval(val(string("i"))).val == string("i");

test bool test02() = eval(val(error("bang!"))).val == error("bang!");

test bool test03() = eval([], var("x"), ("x" : nat(1))).val == nat(1);

test bool test04() = eval(plus(val(nat(1)), val(nat(2)))).val == nat(3);

test bool test05() = eval(seq(val(nat(1)), val(nat(2)))).val == nat(2);

test bool test06() = eval(eq(val(nat(1)), val(nat(1)))).val == nat(1);

test bool test07() = eval(eq(val(nat(1)), val(nat(2)))).val == nat(0);

test bool test08() = eval(cond(eq(val(nat(1)), val(nat(1))), val(nat(1)), val(nat(0)))).val == nat(1);

test bool test08() = eval(cond(eq(val(nat(1)), val(nat(2))), val(nat(1)), val(nat(0)))).val == nat(0);

test bool test09() = eval(block(seq(assign("x", val(nat(1))), val(nat(2))))).val == nat(2);

test bool test10() = eval([], assign("x", val(nat(1))), ("x" : nat(0))).val == nat(1);

test bool test11() = eval(block(seq(assign("x", val(nat(1))), seq(assign("x", val(nat(2))), var("x"))))).val == nat(2);

test bool test12() = eval([fdef("f", ["x"], val(nat(2)))], call("f", [val(nat(1))]), ()).val == nat(2);

test bool test12() = eval([fdef("f", ["x"], var("x"))], call("f", [val(nat(1))]), ()).val == nat(1);

test bool test13() = eval("1 + 2").val == nat(3);

test bool test14() = eval("{ x = 1; y = x + 1; x + y }").val == nat(3);
