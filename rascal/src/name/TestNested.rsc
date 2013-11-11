module name::TestNested


import lang::missgrant::base::AST;
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

loc progloc = |project://Rascal-Hygiene/input/testnested.sim|;
loc x1def = |project://Rascal-Hygiene/input/testnested.sim|(5,1,<1,5>,<1,6>);
loc x1use = |project://Rascal-Hygiene/input/testnested.sim|(12,1,<1,12>,<1,13>);
loc x2def = |project://Rascal-Hygiene/input/testnested.sim|(21,1,<1,21>,<1,22>);
loc x2use = |project://Rascal-Hygiene/input/testnested.sim|(28,1,<1,28>,<1,29>);
loc x3def = |project://Rascal-Hygiene/input/testnested.sim|(37,1,<1,37>,<1,38>);
loc x3use = |project://Rascal-Hygiene/input/testnested.sim|(44,1,<1,44>,<1,45>);

Prog prog() {
  writeFile(progloc, "(var x = 1; x + (var x = 1; x + (var x = 1; x)))");
  return load(progloc);
}

NameGraph resolve() = resolveNames(prog());

NameGraph sNames1() {
  Vs = {x3def, x3use};
  Es = (x3use:x3def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix1() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames1(), resolveNames, name2var);
}
test bool test1() {
  return isCompiledHygienically(sNames1(), resolveNames(fix1()));
}

NameGraph sNames2() {
  Vs = {x2def, x3use};
  Es = (x3use:x2def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix2() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames2(), resolveNames, name2var);
}
test bool test2() {
  return isCompiledHygienically(sNames2(), resolveNames(fix2()));
}


NameGraph sNames3() {
  Vs = {x1def, x3use};
  Es = (x3use:x1def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix3() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames3(), resolveNames, name2var);
}
test bool test3() {
  return isCompiledHygienically(sNames3(), resolveNames(fix3()));
}

NameGraph sNames4() {
  Vs = {x1def, x2use};
  Es = (x2use:x1def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix4() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames4(), resolveNames, name2var);
}
test bool test4() {
  return isCompiledHygienically(sNames4(), resolveNames(fix4()));
}


NameGraph sNames5() {
  Vs = {x2def};
  Es = ();
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix5() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames5(), resolveNames, name2var);
}
test bool test5() {
  return isCompiledHygienically(sNames5(), resolveNames(fix5()));
}

NameGraph sNames6() {
  Vs = {x2def,x2use,x3def,x3use};
  Es = (x3use:x2def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix6() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames6(), resolveNames, name2var);
}
test bool test6() {
  return isCompiledHygienically(sNames6(), resolveNames(fix6()));
}

NameGraph sNames7() {
  Vs = {x2use,x3def};
  Es = (x2use:x3def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix7() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames7(), resolveNames, name2var);
}
test bool test7() {
  return isCompiledHygienically(sNames7(), resolveNames(fix7()));
}


NameGraph sNames8() {
  Vs = {x2use,x3def,x1def};
  Es = (x2use:x3def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix8() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames8(), resolveNames, name2var);
}
test bool test8() {
  return isCompiledHygienically(sNames8(), resolveNames(fix8()));
}

NameGraph sNames9() {
  Vs = {x2use,x3def,x1def,x3use};
  Es = (x2use:x3def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix9() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames9(), resolveNames, name2var);
}
test bool test9() {
  return isCompiledHygienically(sNames9(), resolveNames(fix9()));
}

NameGraph sNames10() {
  Vs = {x1use,x3def};
  Es = (x1use:x3def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix10() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames10(), resolveNames, name2var);
}
test bool test10() {
  return isCompiledHygienically(sNames10(), resolveNames(fix10()));
}

// requires three consecutive renamings (recursive calls of fix)
NameGraph sNames11() {
  Vs = {x2def,x2use,x3def,x3use};
  Es = (x2use:x2def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix11() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames11(), resolveNames, name2var);
}
test bool test11() {
  return isCompiledHygienically(sNames11(), resolveNames(fix11()));
}
