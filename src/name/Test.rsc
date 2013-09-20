module name::Test


import lang::missgrant::base::AST;
import lang::missgrant::base::Implode;
import lang::missgrant::base::NameRel;

import lang::simple::AST;
import lang::simple::Compile;
import lang::simple::Finishing;
import lang::simple::NameRel;
import lang::simple::Pretty;

import name::Relation;
import name::HygienicCorrectness;
import name::VisualizeRelation;

import IO;

Controller statemachine1() = load(|project://MissGrant/input/missgrant.ctl|);
Controller statemachine1illcompiled() = load(|project://MissGrant/input/missgrant-illcompiled.ctl|);

void printCompiled1() = println(pretty(compile(statemachine1())));

Prog unfinishedCompiled1() = compile(statemachine1()); 
Prog compiled1() = finishGenProg(unfinishedCompiled1());

Result names1() = resolveNames(compiled1());

void visualizeOriginal1() = renderNames(resolveNames(statemachine1()));
void visualizeCompiled1() = renderNames(names1());

set[Link] check1() {
  m = statemachine1();
  p = finishGenProg(compile(m));
  return unhygienicLinks(resolveNames(m), resolveNames(p));
}

set[Link] check2() {
  m = statemachine1illcompiled();
  p = finishGenProg(compile(m));
  return unhygienicLinks(resolveNames(m), resolveNames(p));
}
