module name::tests::TestDoor

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

public Controller door1() = load(|project://Rascal-Hygiene/input/door1.ctl|);
public Controller door2() = load(|project://Rascal-Hygiene/input/door2.ctl|);

loc outloc1() = |project://Rascal-Hygiene/output/door1.sim|;
loc outloc2() = |project://Rascal-Hygiene/output/door2.sim|;
loc fixloc1() = |project://Rascal-Hygiene/output/door-fixed1.sim|;
loc fixloc2() = |project://Rascal-Hygiene/output/door-fixed2.sim|;

NameGraph names1() = resolveNames(door1());
NameGraph names2() = resolveNames(door2());

void visNames1() = renderNames(names1());
void visNames2() = renderNames(names2());

Prog compiledProg1() = compile(door1());
Prog compiled1() { 
  Prog p = compiledProg1();
  writeFile(outloc1(), pretty(p));
  return p;
}

Prog compiledProg2() = compile(door2());
Prog compiled2() { 
  Prog p = compiledProg2();
  writeFile(outloc2(), pretty(p));
  return p;
}

NameGraph namesCompiled1() = resolveNames(compiled1());
NameGraph namesCompiled2() = resolveNames(compiled2());

void visNamesCompiled1() = renderNames(namesCompiled1());
void visNamesCompiled2() = renderNames(namesCompiled2());

Prog fixed1() {
  Prog p = nameFix(#Prog, names1(), compiled1(), resolveNames);
  writeFile(fixloc1(), pretty(p));
  return p;
}
Prog fixed2() {
  Prog p = nameFix(#Prog, names2(), compiled2(), resolveNames);
  writeFile(fixloc2(), pretty(p));
  return p;
}

NameGraph namesFixed1() = resolveNames(fixed1());
NameGraph namesFixed2() = resolveNames(fixed2());

void visNamesFixed1() = renderNames(namesFixed1());
void visNamesFixed2() = renderNames(namesFixed2());
