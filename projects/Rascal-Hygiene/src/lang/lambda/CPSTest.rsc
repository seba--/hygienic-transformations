module lang::lambda::CPSTest

import lang::lambda::Syntax;
import lang::lambda::Names;
import lang::lambda::CPS;

import name::HygienicCorrectness;
import name::NameFix;

public Exp testProg1 = parse("(1 + 2) + (1 + 2)");
public Exp testProg2 = parse("lambda y. lambda x0. (y + x0) + (y + x0)");
public Exp testProg3 = parse("((lambda z. (y + 3) + 1 + z) 3) + ((lambda w. (y + 3) + 2 + w) 3)");
public Exp testProg4 = parse("(lambda x. x + 1) 1 + (lambda y. y + 1) 1");
public Exp testProg5 = parse("lambda k0. lambda x0. (x0 + k0 + x0) + (k0 + x0)");

test bool testCPS1() {
  resetNewvar();
  cps = CPS1(testProg1, var("k0"));
  return isHygienic(resolve(testProg1), resolve(cps));
}
test bool testCPS2() {
  resetNewvar();
  cps = CPS1(testProg2, var("k0"));
  return isHygienic(resolve(testProg2), resolve(cps));
}
test bool testCPS3() {
  resetNewvar();
  cps = CPS1(testProg3, var("k0"));
  return isHygienic(resolve(testProg3), resolve(cps));
}
test bool testCPS4() {
  resetNewvar();
  cps = CPS1(testProg4, var("k0"));
  return isHygienic(resolve(testProg4), resolve(cps));
}
test bool testCPS5fail() {
  resetNewvar();
  cps = CPS1(testProg5, var("k0"));
  return !isHygienic(resolve(testProg5), resolve(cps));
}
test bool testCPS5fail() {
  resetNewvar();
  cps = CPS1(testProg5, var("k0"));
  fixed = nameFix(#Exp, resolve(testProg5), cps, resolve);
  return isHygienic(resolve(testProg5), resolve(fixed));
}

