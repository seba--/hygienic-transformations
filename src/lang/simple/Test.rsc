module lang::simple::Test


import lang::missgrant::base::AST;
import lang::missgrant::base::Implode;
import lang::missgrant::base::NameRel;

import lang::simple::AST;
import lang::simple::Compile;
import lang::simple::Finishing;
import lang::simple::NameRel;
import lang::simple::Pretty;
import lang::simple::VisualizeNameRel;

import IO;

Controller statemachine1() = load(|project://MissGrant/input/missgrant.ctl|);

void printCompiled1() = println(pretty(compile(statemachine1())));

Prog compiled1() = finishGenProg(compile(statemachine1()));

Result names1() = resolveNames(compiled1());

void visualizeOriginal1() = renderNames(resolveNames(statemachine1()));
void visualizeCompiled1() = renderNames(names1());