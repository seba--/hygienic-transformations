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

import IO;
import String;
import util::Maybe;

loc statemachine1Loc() = |project://Rascal-Hygiene/input/missgrant.ctl|;
loc statemachine1illcompiledLoc() = |project://Rascal-Hygiene/input/missgrant-illcompiled.ctl|;

Controller statemachine1() = load(statemachine1Loc());
Controller statemachine1illcompiled() = load(statemachine1illcompiledLoc());

loc initStateLoc() = statemachine1().states[0]@location;

void printCompiled1() = println(pretty(compile(statemachine1())));

Prog compiled1() = compile(statemachine1()); 

Prog compiled1ill() = compile(statemachine1illcompiled()); 

set[str] strings(&T t) = { s | /str s := t};
set[tuple[str,lrel[Maybe[loc],str]]] stringsOrigins(&T t) = { <s,origins(s)> | /str s := t};

set[tuple[str,lrel[Maybe[loc],str]]] sourceStrings(loc source, &T t) =
  { result | result:<s,[<just(sloc),_>]> <- stringsOrigins(t), sloc.path == source.path};

set[tuple[str,lrel[Maybe[loc],str]]] synthesizedStrings(loc source, &T t) =
  stringsOrigins(t) - sourceStrings(source, t); 

