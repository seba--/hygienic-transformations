module name::tests::TestSubst

import IO;

import lang::simple::AST;
import lang::simple::Parse;
import lang::simple::Implode;
import lang::simple::NameRel;
import lang::simple::Pretty;

import lang::simple::inline::Subst;

import name::HygienicCorrectness;

private loc substTestFile = |project://Rascal-Hygiene/output/testsubst.sim|;
private str source() = "fun zero() = 0;
               'fun succ(x) = let n = 1 in x + n;
               '
               'let n = x + 5 in 
               '  succ(succ(n + x + zero()))
               '";

private Prog load(str code) {
  writeFile(substTestFile, code);
  return lang::simple::Implode::load(substTestFile);
}
private Prog substProg() = load(source());

Prog subst1() {
  str x = "n";
  Exp e = call("ERROR", []);
  Prog x2 = subst(substProg(), x, e);
  println(pretty(substProg()));
  println(pretty(x2));
  return x2;
}
test bool testSubst1() {
  return subst1() == substProg();
}

private Prog subst2() {
  str x = "x";
  Exp e = call("GOOD", []);
  Prog x2 = lang::simple::inline::Subst::subst(substProg(), x, e);
  println(pretty(x2));
  return x2;
}
test bool testSubst2() {
  return count(call("GOOD", []), subst2()) == 2;
}

private Prog subst3() {
  str x = "x";
  Exp e = var("n");
  Prog x2 =  subst(substProg(), x, e);
  println(pretty(x2));
  return x2;
}
test bool testSubst3() {
  p = subst3();
  nvars = count(var("n"), p);
  bool hygienic = isHygienic(resolveNames(substProg()),resolveNames(p));
  return nvars == 4 && !hygienic;
}

private Prog subst4() {
  str x = "x";
  Exp e = var("n");
  Prog x2 = captureAvoidingSubst(substProg(), x, e);
  println(pretty(x2));
  return x2;
}
test bool testSubst4() {
  p = subst4();
  nvars = count(var("n"), p);
  renamedVars = count(var("n_0"), p);
  bool hygienic = isHygienic(resolveNames(substProg()),resolveNames(p));
  
  return nvars == 3 && renamedVars == 1 && hygienic;
}

private Prog subst5() {
  str x = "x";
  Exp e = times(val(nat(2)), var("n"));
  Prog x2 = captureAvoidingSubst(substProg(), x, e);
  println(pretty(x2));
  return x2;
}
test bool testSubst5() {
  p = subst5();
  nvars = count(var("n"), p);
  renamedVars = count(var("n_0"), p);
  bool hygienic = isHygienic(resolveNames(substProg()),resolveNames(p));
  
  return nvars == 3 && renamedVars == 1 && hygienic;
}

private int count(&T t, &U here) {
  i = 0;
  visit(here) {
    case &T t2: if (t == t2) i = i + 1;
  }
  return i;
}
