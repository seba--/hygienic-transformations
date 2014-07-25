module lang::lambda::CSETest

import IO;

import lang::lambda::Syntax;
import lang::lambda::Names;
import lang::lambda::Equal;
import lang::lambda::CSE;

import name::NameFix;


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


public Exp testProg1 = parse("(1 + 2) + (1 + 2)");
public Exp testProg1res = parse("(lambda x. x + x) (1 + 2)");

public Exp testProg2 = parse("lambda y. lambda x0. (y + x0) + (y + x0)");
public Exp testProg2res = parse("lambda y. lambda z. (lambda x. x + x) (y + z)");

public Exp testProg3 =
  parse("((lambda z. (y + 3) + 1 + z) 3) + ((lambda w. (y + 3) + 2 + w) 3)");
public Exp testProg3res =
  parse("(lambda x. ((lambda z. x + 1 + z) 3) + ((lambda w. x + 2 + w) 3)) (y + 3)");

public Exp testProg4 =
  parse("(lambda x. x + 1) 1 + (lambda y. y + 1) 1");
public Exp testProg4res =
  parse("(lambda x. x + x) ((lambda x. x + 1) 1)");

public Exp testProg5 = parse("lambda y. lambda x0. (x0 + y + x0) + (y + x0)");
public Exp testProg5res = parse("lambda y. lambda x0. (lambda x1. (x0 + x1) + x1) (y + x0)");


test bool testCSE1() = equal(CSE(testProg1), testProg1res);
test bool testCSE2() = equal(CSE(testProg2), testProg2res);
test bool testCSE3() = equal(CSE(testProg3), testProg3res);
test bool testCSE4() = equal(CSE(testProg4), testProg4res);
test bool testCSE5() = !equal(CSE(testProg5), testProg5res);
test bool testCSE5() {
  fixed = nameFix(#Exp, resolve(testProg5), CSE(testProg5), resolve);
  return equal(fixed, testProg5res);
}
