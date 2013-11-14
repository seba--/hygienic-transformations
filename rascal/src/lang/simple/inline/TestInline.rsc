module lang::simple::inline::TestInline

import IO;
import Set;

import lang::simple::AST;
import lang::simple::Parse;
import lang::simple::Implode;
import lang::simple::NameRel;

import lang::simple::inline::Inline;

import name::HygienicCorrectness;

loc testfile = |project://Rascal-Hygiene/output/testinline.sim|;
str source() = "zero() = 0; \nsucc(x) = {var n = 1; x + n};    \n\n(var n = free + 5; succ(succ(n + free + zero())))";
loc testfile2 = |project://Rascal-Hygiene/output/testinline2.sim|;
str source2() = "zero() = 0; \ndouble(x) = {var n = 0; n = x; n + n};    \n\n(var n = free + 5; double(double(n + free + zero())))";

Prog load(loc file, str code) {
  writeFile(file, code);
  return load(file);
}
Prog prog() = load(testfile, source());
Prog prog2() = load(testfile2, source2());

Prog inline1() {
  return inline(prog(), "zero");
}
test bool testInline1() {
  return count(val(nat(0)), inline1()) == 2;
}

Prog inline2() {
  return inline(prog(), "succ");
}
test bool testInline2() {
  p = inline2();
  nvars = count(var("n"), p);
  hygienic = isCompiledHygienically(resolveNames(prog()),resolveNames(p));
  return nvars == 4 && !hygienic;
}

Prog inline3() {
  return captureAvoidingInline(prog(), "succ");
}
test bool testInline3() {
  p = inline3();
  nvars = count(var("n"), p);
  nRenamed = count(var("n_0"), p);
  hygienic = isCompiledHygienically(resolveNames(prog()),resolveNames(p));
  
  G = resolveNames(p);
  renamedDefs = { d | <d,name> <- G.N<0,1>, name == "n_0"};
  references = { { u | u <- G.E<0>, G.E[u] == d} | d <- renamedDefs };
  allRenamedDefsHaveOneRef = (true | it && size(refs) == 1 | refs <- references);
  
  return nvars == 1 && nRenamed == 3 && hygienic && allRenamedDefsHaveOneRef;
}

Prog inline4() {
  return inline(prog2(), "double");
}
test bool testInline4() {
  p = inline4();
  nvars = count(var("n"), p);
  hygienic = isCompiledHygienically(resolveNames(prog2()),resolveNames(p));
  return nvars == 7 && !hygienic;
}

Prog inline5() {
  return captureAvoidingInline(prog2(), "double");
}
test bool testInline5() {
  p = inline5();
  nvars = count(var("n"), p);
  nRenamed = count(var("n_0"), p);
  hygienic = isCompiledHygienically(resolveNames(prog2()),resolveNames(p));
  
  G = resolveNames(p);
  renamedDefs = { d | <d,name> <- G.N<0,1>, name == "n_0"};
  references = { { u | u <- G.E<0>, G.E[u] == d} | d <- renamedDefs };
  allRenamedDefsHaveThreeRefs = (true | it && size(refs) == 3 | refs <- references);
  
  return nvars == 1 && nRenamed == 6 && hygienic && allRenamedDefsHaveThreeRefs;
}

Prog inline6() {
  return captureAvoidingInline2(prog(), "succ");
}
test bool testInline6() {
  p = inline6();
  nvars = count(var("n"), p);
  nRenamed = count(var("n_0_0"), p);
  hygienic = isCompiledHygienically(resolveNames(prog()),resolveNames(p));
  
  G = resolveNames(p);
  renamedDefs = { d | <d,name> <- G.N<0,1>, name == "n_0_0"};
  references = { { u | u <- G.E<0>, G.E[u] == d} | d <- renamedDefs };
  allRenamedDefsHaveOneRef = (true | it && size(refs) == 1 | refs <- references);
  
  return nvars == 2 && nRenamed == 2 && hygienic && allRenamedDefsHaveOneRef;
}

Prog inline7() {
  return captureAvoidingInline2(prog2(), "double");
}
test bool testInline7() {
  p = inline7();
  nvars = count(var("n"), p);
  nRenamed = count(var("n_0_0"), p);
  hygienic = isCompiledHygienically(resolveNames(prog2()),resolveNames(p));
  
  G = resolveNames(p);
  renamedDefs = { d | <d,name> <- G.N<0,1>, name == "n_0_0"};
  references = { { u | u <- G.E<0>, G.E[u] == d} | d <- renamedDefs };
  allRenamedDefsHaveThreeRefs = (true | it && size(refs) == 3 | refs <- references);
  
  return nvars == 3 && nRenamed == 4 && hygienic && allRenamedDefsHaveThreeRefs;
}


int count(&T t, &U here) {
  i = 0;
  visit(here) {
    case &T t2: if (t == t2) i = i + 1;
  }
  return i;
}
