module name::TestString


import lang::missgrant::base::AST;
import lang::missgrant::base::Implode;
import lang::missgrant::base::NameRel;

import lang::simple::AST;
import lang::simple::Compile;
import lang::simple::Implode;
import lang::simple::NameRel;
import lang::simple::Parse;
import lang::simple::Pretty;

import name::Relation;
import name::HygienicCorrectness;
import name::VisualizeRelation;
import name::Rename;
import name::Names;

import IO;
import Set;

Controller statemachine1() = load(|project://Rascal-Hygiene/input/missgrant.ctl|);
Controller statemachine1illcompiled() = load(|project://Rascal-Hygiene/input/missgrant-illcompiled.ctl|);

loc initStateLoc() = statemachine1().states[0]@location;

void printCompiled1() = println(srcCompiled1());

str srcCompiled1() = pretty(compile(statemachine1()));

str srcIllCompiled1() = pretty(compile(statemachine1illcompiled()));

Prog compiled1() = compile(statemachine1()); 

Prog compiled1ill() = compile(statemachine1illcompiled()); 

NameGraph names1() = resolveNames(compiled1());

Edges check1() {
  m = statemachine1();
  p = compiled1();
  return unhygienicLinks(resolveNames(m), resolveNames(p));
}

Edges check2() {
  m = statemachine1illcompiled();
  p = compiled1ill();
  return unhygienicLinks(resolveNames(m), resolveNames(p));
}

Controller renameS1() {
  m = statemachine1();
  init = m.states[0];
  return concatRename(resolveNames(m), m, getID(init.name), "-renamed");
}

Prog renameP1() {
  p = compiled1();
  d0 = p.sig[0].name;
  return concatRename(resolveNames(p), p, getID(d0), "-renamed");
}

str testProg1code() = "
'define foo(x) = x + x;
'define bar(foo) = bar(foo);
'x = foo(bar(2))
";
loc testProg1loc() = |project://Rascal-Hygiene/input/testProg1.sim|;
loc testProg1() {
  writeFile(testProg1loc(), testProg1code());
  return testProg1loc();
}

Prog renameTestProg1(str(Prog) from) {
  p = implodeProg(parse(testProg1()));
  x = from(p);
  return concatRename(resolveNames(p), p, getID(x), "-renamed");
}
 
Prog renameProg1()  = renameTestProg1(
   str(Prog p)   { return p.sig[0].name; });

Prog renameProg2()  = renameTestProg1(
   str(Prog p)   { return p.sig[0].body.e1.x; });

Prog renameProg3()  = renameTestProg1(
   str(Prog p)   { return p.sig[1].name; });


Prog fixTheHygiene(Controller m) {
  Prog p = compile(m);
  sNames = resolveNames(m);
  tNames = resolveNames(p);
  p2 = fixHygiene(m, p, resolveNames, resolveNames);
  assert isCompiledHygienically(sNames, resolveNames(p2)) : "unhygienic links: <unhygienicLinks(sNames, resolveNames(p2))>";
  return p2;
}

Prog fixHygiene1() = fixTheHygiene(statemachine1());
Prog fixHygiene2() = fixTheHygiene(statemachine1illcompiled());

test bool rand(Controller m) {
  p = finishGenProg(compile(m));
  sNames = resolveNames(m);
  tNames = resolveNames(p);
  p2 = fixHygiene(m, p, resolveNames, resolveNames, name2var);
  return isCompiledHygienically(sNames, resolveNames(p2));
} 
