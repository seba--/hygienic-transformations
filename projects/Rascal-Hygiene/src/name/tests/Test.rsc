module name::tests::Test

import lang::missgrant::base::AST;
import lang::missgrant::base::Implode;
import lang::missgrant::base::NameRel;

import lang::simple::AST;
import lang::simple::Compile;
import lang::simple::Implode;
import lang::simple::NameRel;
import lang::simple::Parse;
import lang::simple::Pretty;

import name::NameGraph;
import name::HygienicCorrectness;
import name::figure::VisualizeRelation;
import name::NameFix;

import IO;

Controller statemachine1() = load(|project://Rascal-Hygiene/input/missgrant.ctl|);
Controller statemachine1illcompiled() = load(|project://Rascal-Hygiene/input/missgrant-illcompiled.ctl|);
loc statemachine1Out() = |project://Rascal-Hygiene/output/missgrant.sim|;
loc statemachine1illcompiledOut() = |project://Rascal-Hygiene/output/missgrant-illcompiled.sim|;

loc initStateLoc() = statemachine1().states[0]@location;

void printCompiled1() = println(pretty(compile(statemachine1())));

Prog compiled1() {
   p = compile(statemachine1());
   writeFile(statemachine1Out(), pretty(p));
   return p;
}

Prog compiled1ill() = compile(statemachine1illcompiled());


NameGraph names1() = resolveNames(compiled1());

void visualizeOriginal1() = renderNames(resolveNames(statemachine1()));
void visualizeCompiled1() = renderNames(names1());

set[Edge] check1() {
  m = statemachine1();
  p = compile(m);
  return findCapture(resolveNames(m), resolveNames(p));
}

set[Edge] check2() {
  m = statemachine1illcompiled();
  p = compile(m);
  return findCapture(resolveNames(m), resolveNames(p));
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
  new = "<d0.name>-renamed";
  return rename(resolveNames(p), p, d0@location, new);
}

str testProg1code() = "
'fun foo(x) = x + x;
'fun bar(foo) = bar(foo);
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
  new = "<d0.name>-renamed";
  return rename(resolveNames(p), p, d0@location, new);
}

Prog renameProg2() {
  p = implodeProg(parse(testProg1()));
  d0 = p.defs[0].body.e1.x;
  new = "<d0.name>-renamed";
  return rename(resolveNames(p), p, d0@location, new);
}

Prog renameProg3() {
  p = implodeProg(parse(testProg1()));
  d0 = p.defs[1].name;
  new = "<d0.name>-renamed";
  return rename(resolveNames(p), p, d0@location, new);
}



&T nameFix1() {
  m = statemachine1();
  Prog p = compile(m);
  sNames = resolveNames(m);
  tNames = resolveNames(p);
  p2 = nameFix(#Prog, sNames, p, resolveNames);
  assert isCompiledHygienically(sNames, resolveNames(p2)) : "unhygienic links: <findCapture(sNames, resolveNames(p2))>";
  return p2;
}

&T nameFix2() {
  m = statemachine1illcompiled();
  p = compile(m);
  sNames = resolveNames(m);
  tNames = resolveNames(p);
  p2 = nameFix(#Prog, sNames, p, resolveNames);
  assert isCompiledHygienically(sNames, resolveNames(p2)) : "unhygienic links: <findCapture(sNames, resolveNames(p2))>";
  return p2;
}
