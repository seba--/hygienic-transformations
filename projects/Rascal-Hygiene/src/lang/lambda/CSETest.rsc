module lang::lambda::CSETest

import IO;

import lang::lambda::Syntax;
import lang::lambda::Names;
import lang::lambda::CSE;

public Exp testProg1 = parse("(1 + 2) + (1 + 2)");
public Exp testProg1res = parse("(lambda x. x + x) (1 + 2)");

public Exp testProg2 = parse("lambda y. lambda x0. (y + x0) + (y + x0)");
public Exp testProg2res = parse("lambda y. lambda z. (lambda x. x + x) (y + z)");

public Exp testProg3 =
  parse("((lambda z. (y + 3) + 1 + z) 3) + ((lambda w. (y + 3) + 2 + w) 3)");
public Exp testProg3res =
  parse("(lambda x. ((lambda z. x + z) 3) + ((lambda w. x + w) 3)) (y + 3)");

public Exp testProg4 =
  parse("(lambda x. x + 1) 1 + (lambda y. y + 1) 1");
public Exp testProg4res =
  parse("(lambda x. x + x) ((lambda x. x + 1) 1)");

public Exp testProg5 = parse("lambda y. lambda x0. (x0 + y + x0) + (y + x0)");
public Exp testProg5res = parse("lambda y. lambda x0. (lambda x. (x0 + x) + x) (y + z)");

  
test bool equal1() {
  p1 = parse("lambda x. x");
  p2 = parse("lambda y. y");
  return equal(p1,p2);
}
test bool equal2() {
  p1 = parse("lambda y. lambda x. x");
  p2 = parse("lambda x. lambda y. y");
  return equal(p1,p2);
}
test bool equal3() {
  p1 = parse("lambda y. lambda x. y");
  p2 = parse("lambda x. lambda y. x");
  return equal(p1,p2);
}
test bool equal4() {
  p1 = parse("lambda y. lambda x. x");
  p2 = parse("lambda x. lambda y. x");
  return !equal(p1,p2);
}
