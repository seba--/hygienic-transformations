module lang::simple::inline::TestSubst

import IO;

import lang::simple::AST;
import lang::simple::Parse;
import lang::simple::Implode;
import lang::simple::NameRel;
import lang::simple::Pretty;

import lang::simple::inline::Subst;

import name::HygienicCorrectness;

loc testfile = |project://Rascal-Hygiene/output/testsubst.sim|;
str source() = "fun zero() = 0;
               'fun succ(x) = {var n = 1; x + n};
               '
               '{
               '  var n = free + 5; 
               '  succ(succ(n + free + zero()))
               '}";

Prog load(str code) {
  writeFile(testfile, code);
  return load(testfile);
}
Prog prog() = load(source());

Prog subst1() {
  x = "n";
  e = call("ERROR", []);
  x2 = subst(prog(), x, e);
  println(pretty(x2));
  return x2;
}
test bool testSubst1() {
  return subst1() == prog();
}

Prog subst2() {
  x = "free";
  e = call("GOOD", []);
  x2 = subst(prog(), x, e);
  println(pretty(x2));
  return x2;
}
test bool testSubst2() {
  return count(call("GOOD", []), subst2()) == 2;
}

Prog subst3() {
  x = "free";
  e = var("n");
  x2 =  subst(prog(), x, e);
  println(pretty(x2));
  return x2;
}
test bool testSubst3() {
  p = subst3();
  nvars = count(var("n"), p);
  hygienic = isCompiledHygienically(resolveNames(prog()),resolveNames(p));
  return nvars == 4 && !hygienic;
}

Prog subst4() {
  x = "free";
  e = var("n");
  x2 = captureAvoidingSubst(prog(), x, e);
  println(pretty(x2));
  return x2;
}
test bool testSubst4() {
  p = subst4();
  nvars = count(var("n"), p);
  renamedVars = count(var("n_0"), p);
  hygienic = isCompiledHygienically(resolveNames(prog()),resolveNames(p));
  
  return nvars == 3 && renamedVars == 1 && hygienic;
}

int count(&T t, &U here) {
  i = 0;
  visit(here) {
    case &T t2: if (t == t2) i = i + 1;
  }
  return i;
}
