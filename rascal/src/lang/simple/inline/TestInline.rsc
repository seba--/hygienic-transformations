module lang::simple::inline::TestInline

import IO;
import Set;

import lang::simple::AST;
import lang::simple::Parse;
import lang::simple::Implode;
import lang::simple::Pretty;
import lang::simple::NameRel;

import lang::simple::inline::Inline;

import name::HygienicCorrectness;

loc testfile = |project://Rascal-Hygiene/output/testinline.sim|;
str source() = "fun zero() = 0; 
               'fun succ(x) = {var n = 1; x + n}; 
               '{
               '  var n = free + 5; 
               '  succ(succ(n + free + zero()))
               '}";
               
loc testfile2 = |project://Rascal-Hygiene/output/testinline2.sim|;
str source2() = "fun zero() = 0; 
                'fun double(x) = {var n = 0; (n = x; n + n)};
                '{
                '  var n = free + 5; 
                '  double(double(n + free + zero()))
                '}";


loc testfile3 = |project://Rascal-Hygiene/output/testinline3.sim|;
str source3() = "fun zero() = 0; 
                'fun succ(x) = {var n = 1; x + n + zero()};
                'fun zero() = 2; 
                '{
                '  var n = free + 5; 
                '  succ(succ(n + free + zero()))
                '}";

loc andOrFile = |project://Rascal-Hygiene/output/andOr.sim|;
str andOrSource() =   
                "fun or(x, y) = { var tmp = x; if tmp == 0 then y else tmp };
                'fun and(x, y) = !or(!x, !y);
                '{
                '  var or = 1;
                '  { var tmp = 0; 
                '    and(or, tmp) }
                '}";

Prog load(loc file, str code) {
  writeFile(file, code);
  return load(file);
}
Prog prog() = load(testfile, source());
Prog prog2() = load(testfile2, source2());
Prog prog3() = load(testfile3, source3());
Prog andOr() = load(andOrFile, andOrSource());

Prog inline1() {
  x = inline(prog(), "zero");
  println(pretty(x));
  return x;
}
test bool testInline1() {
  return count(val(nat(0)), inline1()) == 2;
}

Prog inline2() {
  x = inline(prog(), "succ");
  println(pretty(x));
  return x;
}
test bool testInline2() {
  p = inline2();
  nvars = count(var("n"), p);
  hygienic = isCompiledHygienically(resolveNames(prog()),resolveNames(p));
  return nvars == 4 && !hygienic;
}

Prog inline3() {
  x = captureAvoidingInline(prog(), "succ");
  println(pretty(x));
  return x;
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
  x = inline(prog2(), "double");
  println(pretty(x));
  return x;
}
test bool testInline4() {
  p = inline4();
  nvars = count(var("n"), p);
  hygienic = isCompiledHygienically(resolveNames(prog2()),resolveNames(p));
  return nvars == 7 && !hygienic;
}

Prog inline5() {
  x = captureAvoidingInline(prog2(), "double");
  println(pretty(x));
  return x;
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
  x = captureAvoidingInline2(prog(), "succ");
  println(pretty(x));
  return x;
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
  x = captureAvoidingInline2(prog2(), "double");
  println(pretty(x));
  return x;
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


Prog inline8() {
  x = captureAvoidingInline(prog3(), "succ");
  println(pretty(x));
  return x;
}
test bool testInline8() {
  p = inline8();
  nvars = count(var("n"), p) + count(call("zero", []), p);
  nRenamed = count(var("n_0"), p) + count(call("zero_0", []), p);
  hygienic = isCompiledHygienically(resolveNames(prog3()),resolveNames(p));
  
  G = resolveNames(p);
  renamedDefs = { d | <d,name> <- G.N<0,1>, name in {"n_0", "zero_0"}};
  references = { { u | u <- G.E<0>, G.E[u] == d} | d <- renamedDefs };
  allRenamedDefsHaveOneRef = (true | it && size(refs) == 1 | refs <- references);
  
  println("nvars == 2: <nvars>");
  println("nRenamed == 2: <nRenamed>");
  println("hyg = <hygienic>"); 
  println("allRenamedDefsHaveOneRef = <allRenamedDefsHaveOneRef>");
  return nvars == 4 && nRenamed == 4 && hygienic && allRenamedDefsHaveOneRef;
}

Prog inlineAnd() {
  x = captureAvoidingInline(andOr(), "and");
  //x = captureAvoidingInline(x, "or");
  println(pretty(x));
  return x;
}
test bool testInlineAnd() {
  p = inlineAnd();
  nvars = count(call("and", []), p);
  nRenamed = count(call("and", []), p);
  hygienic = isCompiledHygienically(resolveNames(andOr()),resolveNames(p));
  
  G = resolveNames(p);
  renamedDefs = { d | <d,name> <- G.N<0,1>, name in {"and"}};
  references = { { u | u <- G.E<0>, G.E[u] == d} | d <- renamedDefs };
  allRenamedDefsHaveOneRef = (true | it && size(refs) == 1 | refs <- references);
  
  println("nvars == 2: <nvars>");
  println("nRenamed == 2: <nRenamed>");
  println("hyg = <hygienic>"); 
  println("allRenamedDefsHaveOneRef = <allRenamedDefsHaveOneRef>");
  return nvars == 4 && nRenamed == 4 && hygienic && allRenamedDefsHaveOneRef;
}

Prog inlineAndThenOr() {
  x = captureAvoidingInline(andOr(), "and");
  x = captureAvoidingInline(x, "or");
  println(pretty(x));
  return x;
}
test bool testInlineAndThenOr() {
  p = inlineAndThenOr();
  nvars = count(call("and", []), p);
  nRenamed = count(call("and", []), p);
  hygienic = isCompiledHygienically(resolveNames(andOr()),resolveNames(p));
  
  G = resolveNames(p);
  renamedDefs = { d | <d,name> <- G.N<0,1>, name in {"and"}};
  references = { { u | u <- G.E<0>, G.E[u] == d} | d <- renamedDefs };
  allRenamedDefsHaveOneRef = (true | it && size(refs) == 1 | refs <- references);
  
  println("nvars == 2: <nvars>");
  println("nRenamed == 2: <nRenamed>");
  println("hyg = <hygienic>"); 
  println("allRenamedDefsHaveOneRef = <allRenamedDefsHaveOneRef>");
  return nvars == 4 && nRenamed == 4 && hygienic && allRenamedDefsHaveOneRef;
}


Prog inlineAndThenOrThenNot() {
  x = captureAvoidingInline(andOr(), "and");
  x = captureAvoidingInline(x, "or");
  x = captureAvoidingInline(x, "not");
  println(pretty(x));
  return x;
}
test bool testInlineAndThenOrThenNot() {
  p = inlineAndThenOrThenNot();
  nvars = count(call("and", []), p);
  nRenamed = count(call("and", []), p);
  hygienic = isCompiledHygienically(resolveNames(andOr()),resolveNames(p));
  
  G = resolveNames(p);
  renamedDefs = { d | <d,name> <- G.N<0,1>, name in {"and"}};
  references = { { u | u <- G.E<0>, G.E[u] == d} | d <- renamedDefs };
  allRenamedDefsHaveOneRef = (true | it && size(refs) == 1 | refs <- references);
  
  println("nvars == 2: <nvars>");
  println("nRenamed == 2: <nRenamed>");
  println("hyg = <hygienic>"); 
  println("allRenamedDefsHaveOneRef = <allRenamedDefsHaveOneRef>");
  return nvars == 4 && nRenamed == 4 && hygienic && allRenamedDefsHaveOneRef;
}


Prog inlineOr() {
  x = captureAvoidingInline(andOr(), "or");
  //x = captureAvoidingInline(x, "or");
  println(pretty(x));
  return x;
}
test bool testInlineOr() {
  p = inlineOr();
  nvars = count(call("or", []), p);
  nRenamed = count(call("or", []), p);
  hygienic = isCompiledHygienically(resolveNames(andOr()),resolveNames(p));
  
  G = resolveNames(p);
  renamedDefs = { d | <d,name> <- G.N<0,1>, name in {"or"}};
  references = { { u | u <- G.E<0>, G.E[u] == d} | d <- renamedDefs };
  allRenamedDefsHaveOneRef = (true | it && size(refs) == 1 | refs <- references);
  
  println("nvars == 2: <nvars>");
  println("nRenamed == 2: <nRenamed>");
  println("hyg = <hygienic>"); 
  println("allRenamedDefsHaveOneRef = <allRenamedDefsHaveOneRef>");
  return nvars == 4 && nRenamed == 4 && hygienic && allRenamedDefsHaveOneRef;
}


Prog inlineOrThenAnd() {
  x = captureAvoidingInline(andOr(), "or");
  x = captureAvoidingInline(x, "and");
  println(pretty(x));
  return x;
}
test bool testInlineOrThenAnd() {
  p = inlineOrThenAnd();
  nvars = count(call("or", []), p);
  nRenamed = count(call("or", []), p) + count(call("and", []), p);
  hygienic = isCompiledHygienically(resolveNames(andOr()),resolveNames(p));
  
  G = resolveNames(p);
  renamedDefs = { d | <d,name> <- G.N<0,1>, name in {"or"}};
  references = { { u | u <- G.E<0>, G.E[u] == d} | d <- renamedDefs };
  allRenamedDefsHaveOneRef = (true | it && size(refs) == 1 | refs <- references);
  
  println("nvars == 2: <nvars>");
  println("nRenamed == 2: <nRenamed>");
  println("hyg = <hygienic>"); 
  println("allRenamedDefsHaveOneRef = <allRenamedDefsHaveOneRef>");
  return nvars == 4 && nRenamed == 4 && hygienic && allRenamedDefsHaveOneRef;
}


int count(&T t, &U here) {
  i = 0;
  visit(here) {
    case &T t2: if (t == t2) i = i + 1;
  }
  return i;
}
