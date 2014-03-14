module name::tests::TestInline

import IO;
import Set;

import lang::simple::AST;
import lang::simple::Parse;
import lang::simple::Implode;
import lang::simple::Pretty;
import lang::simple::NameRel;

import lang::simple::inline::Inline;

import name::HygienicCorrectness;
import name::NameGraph;

import Map;

loc testfile = |project://Rascal-Hygiene/output/testinline.sim|;
str source() = "fun zero() = 0; 
               'fun succ(x) = let n = 1 in x + n; 
               'let n = free + 5 in 
               '  succ(succ(n + free + zero()))
               '";
               
loc testfile2 = |project://Rascal-Hygiene/output/testinline2.sim|;
str source2() = "fun zero() = 0; 
                'fun double(x) = let n = 0 in (n = x; n + n);
                'let  n = free + 5 in
                '  double(double(n + free + zero()))
                '";


loc testfile3 = |project://Rascal-Hygiene/output/testinline3.sim|;
str source3() = "fun zero() = 0; 
                'fun succ(x) = let n = 1 in x + n + zero();
                'fun zero() = 2; 
                'let n = free + 5 in 
                '  succ(succ(n + free + zero()))
                '";

loc andOrFile = |project://Rascal-Hygiene/output/andOr.sim|;
str andOrSource() =   
                "fun or(x, y) =  let tmp = x in if tmp == 0 then y else tmp;
                'fun and(x, y) = !or(!x, !y);
                'let or = 1 in
                '  let tmp = 0 in 
                '    and(or, tmp)
                '";

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
  renamedDefs = { d | <d,name> <- piOf(G, p), name == "n_0", d in range(G.E) };
  references = { { u | u <- G.E<0>, G.E[u] == d} | d <- renamedDefs };
  allRenamedDefsHaveOneRef = (true | it && size(refs) == 1 | refs <- references);
  
  println(nvars);
  println(nRenamed);
  println(hygienic);
  println(allRenamedDefsHaveOneRef);
  println(renamedDefs);
  println(references);
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
  renamedDefs = { d | <d,name> <- piOf(G, p), name == "n_0", d in range(G.E)};
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
  hygienic = isCompiledHygienically(resolveNames(prog()),resolveNames(p));
  return hygienic;  
}

Prog inline7() {
  x = captureAvoidingInline2(prog2(), "double");
  println(pretty(x));
  return x;
}
test bool testInline7() {
  p = inline7();
  hygienic = isCompiledHygienically(resolveNames(prog2()),resolveNames(p));
  return hygienic;
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
  renamedDefs = { d | <d,name> <- piOf(G, p), name in {"n_0", "zero_0"}, d in range(G.E)};
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
  hygienic = isCompiledHygienically(resolveNames(andOr()),resolveNames(p));
  return hygienic;
}

Prog inlineImplies() {
  x = captureAvoidingInline(andOr(), "implies");
  //x = captureAvoidingInline(x, "or");
  println(pretty(x));
  return x;
}
test bool testInlineImplies() {
  p = inlineImplies();
  hygienic = isCompiledHygienically(resolveNames(andOr()),resolveNames(p));
  return hygienic;
}

Prog inlineAndThenOr() {
  println("input:");
  println(pretty(andOr()));


  y = inline(andOr(), "and");
  y = inline(y, "or");
  println("Unh inline:");
  println(pretty(y));

  x = captureAvoidingInline(andOr(), "and");
  println("Inlined and");
  println(pretty(x));
  x = captureAvoidingInline(x, "or");
  println("Inlined or");
  println(pretty(x));
  return x;
}
test bool testInlineAndThenOr() {
  p = inlineAndThenOr();
  hygienic = isCompiledHygienically(resolveNames(andOr()),resolveNames(p));
  return hygienic;
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
  hygienic = isCompiledHygienically(resolveNames(andOr()),resolveNames(p));
  return hygienic;
}


Prog inlineOr() {
  x = captureAvoidingInline(andOr(), "or");
  //x = captureAvoidingInline(x, "or");
  println(pretty(x));
  return x;
}
test bool testInlineOr() {
  p = inlineOr();
  hygienic = isCompiledHygienically(resolveNames(andOr()),resolveNames(p));
  return hygienic;
}


Prog inlineOrThenAnd() {
  x = captureAvoidingInline(andOr(), "or");
  x = captureAvoidingInline(x, "and");
  println(pretty(x));
  return x;
}
test bool testInlineOrThenAnd() {
  p = inlineOrThenAnd();
  hygienic = isCompiledHygienically(resolveNames(andOr()),resolveNames(p));
  return hygienic;
}


int count(&T t, &U here) {
  i = 0;
  visit(here) {
    case &T t2: if (t == t2) i = i + 1;
  }
  return i;
}
