module name::Test


import lang::missgrant::base::AST;
import lang::missgrant::base::Implode;
import lang::missgrant::base::NameRel;

import lang::simple::AST;
import lang::simple::Compile;
import lang::simple::Finishing;
import lang::simple::Implode;
import lang::simple::NameRel;
import lang::simple::Parse;
import lang::simple::Pretty;

import name::Relation;
import name::HygienicCorrectness;
import name::VisualizeRelation;
import name::Rename;

import IO;

Controller statemachine1() = load(|project://Rascal-Hygiene/input/missgrant.ctl|);
Controller statemachine1illcompiled() = load(|project://Rascal-Hygiene/input/missgrant-illcompiled.ctl|);

loc initStateLoc() = statemachine1().states[0]@location;

void printCompiled1() = println(pretty(compile(statemachine1())));

Prog unfinishedCompiled1() = compile(statemachine1()); 
Prog compiled1() = finishGenProg(unfinishedCompiled1());

Prog unfinishedCompiled1ill() = compile(statemachine1illcompiled()); 
Prog compiled1ill() = finishGenProg(unfinishedCompiled1ill());


NameGraph names1() = resolveNames(compiled1());

void visualizeOriginal1() = renderNames(resolveNames(statemachine1()));
void visualizeCompiled1() = renderNames(names1());

set[Edge] check1() {
  m = statemachine1();
  p = finishGenProg(compile(m));
  return unhygienicLinks(resolveNames(m), resolveNames(p));
}

set[Edge] check2() {
  m = statemachine1illcompiled();
  p = finishGenProg(compile(m));
  return unhygienicLinks(resolveNames(m), resolveNames(p));
}


Controller renameS1() {
  m = statemachine1();
  init = m.states[0];
  new = state("<init.name>-renamed", init.actions, init.transitions);
  return rename(resolveNames(m), m, init@location, new);
}

Prog renameP1() {
  p = compiled1();
  d0 = p.defs[0].name;
  new = sym("<d0.name>-renamed");
  return rename(resolveNames(p), p, d0@location, new);
}

str testProg1code() = "
'define foo(x) = x + x;
'define bar(foo) = bar(foo);
'x = foo(bar(2))
";
loc testProg1loc() = |project://MissGrant/input/testProg1.sim|;
loc testProg1() {
  writeFile(testProg1loc(), testProg1code());
  return testProg1loc();
}
 
Prog renameProg1() {
  p = implodeProg(parse(testProg1()));
  d0 = p.defs[0].name;
  new = sym("<d0.name>-renamed");
  return rename(resolveNames(p), p, d0@location, new);
}

Prog renameProg2() {
  p = implodeProg(parse(testProg1()));
  d0 = p.defs[0].body.e1.x;
  new = sym("<d0.name>-renamed");
  return rename(resolveNames(p), p, d0@location, new);
}

Prog renameProg3() {
  p = implodeProg(parse(testProg1()));
  d0 = p.defs[1].name;
  new = sym("<d0.name>-renamed");
  return rename(resolveNames(p), p, d0@location, new);
}

//Prog fixHygiene1() {
//  m = statemachine1();
//  p = finishGenProg(compile(m));
//  sNames = resolveNames(m);
//  tNames = resolveNames(p);
//  p2 = fixHygiene(sNames, tNames, p, name2var);
//  assert isCompiledHygienically(sNames, resolveNames(p2)) : "unhygienic links: <unhygienicLinks(sNames, resolveNames(p2))>";
//  return p2;
//}
//
//Prog fixHygiene2() {
//  m = statemachine1illcompiled();
//  p = finishGenProg(compile(m));
//  sNames = resolveNames(m);
//  tNames = resolveNames(p);
//  p2 = fixHygiene(sNames, tNames, p, name2var);
//  //println(pretty(p2));
//  assert isCompiledHygienically(sNames, resolveNames(p2)) : "unhygienic links: <unhygienicLinks(sNames, resolveNames(p2))>";
//  return p2;
//}

&T fixHygiene1() {
  m = statemachine1();
  Prog p = finishGenProg(compile(m));
  sNames = resolveNames(m);
  tNames = resolveNames(p);
  p2 = fixHygiene(m, p, resolveNames, resolveNames, name2var);
  assert isCompiledHygienically(sNames, resolveNames(p2)) : "unhygienic links: <unhygienicLinks(sNames, resolveNames(p2))>";
  return p2;
}

&T fixHygiene2() {
  m = statemachine1illcompiled();
  p = finishGenProg(compile(m));
  sNames = resolveNames(m);
  tNames = resolveNames(p);
  p2 = fixHygiene(m, p, resolveNames, resolveNames, name2var);
  assert isCompiledHygienically(sNames, resolveNames(p2)) : "unhygienic links: <unhygienicLinks(sNames, resolveNames(p2))>";
  return p2;
}

//set[Link] check2() {
//  m = statemachine1illcompiled();
//  p = finishGenProg(compile(m));
//  return unhygienicLinks(resolveNames(m), resolveNames(p));
//}

//test bool rand(Controller m) {
//  p = finishGenProg(compile(m));
//  sNames = resolveNames(m);
//  tNames = resolveNames(p);
//  p2 = fixHygiene(m, p, resolveNames, resolveNames, name2var);
//  return isCompiledHygienically(sNames, resolveNames(p2));
//} 
