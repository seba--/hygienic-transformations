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
loc x1def = |project://Rascal-Hygiene/input/testnested.sim|(1,1,<1,1>,<1,2>);
loc x1use = |project://Rascal-Hygiene/input/testnested.sim|(4,1,<1,4>,<1,5>);
loc x2def = |project://Rascal-Hygiene/input/testnested.sim|(9,1,<1,9>,<1,10>);
loc x2use = |project://Rascal-Hygiene/input/testnested.sim|(12,1,<1,12>,<1,13>);
loc x3def = |project://Rascal-Hygiene/input/testnested.sim|(17,1,<1,17>,<1,18>);
loc x3use = |project://Rascal-Hygiene/input/testnested.sim|(20,1,<1,20>,<1,21>);

Prog prog() {
  writeFile(progloc, "{x: x + {x: x + {x: x}}}");
  return load(progloc);
}

NameGraph resolve() = resolveNames(prog());

NameGraph sNames1(n) {
  Vs = {x3def, x3use};
  Es = (x3use:x3def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix1() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames1, resolveNames, name2var);
}
test bool test1() {
  return isCompiledHygienically(sNames1(0), resolveNames(fix1()));
}

NameGraph sNames2(n) {
  Vs = {x2def, x3use};
  Es = (x3use:x2def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix2() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames2, resolveNames, name2var);
}
test bool test2() {
  return isCompiledHygienically(sNames2(0), resolveNames(fix2()));
}


NameGraph sNames3(n) {
  Vs = {x1def, x3use};
  Es = (x3use:x1def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix3() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames3, resolveNames, name2var);
}
test bool test3() {
  return isCompiledHygienically(sNames3(0), resolveNames(fix3()));
}

NameGraph sNames4(n) {
  Vs = {x1def, x2use};
  Es = (x2use:x1def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix4() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames4, resolveNames, name2var);
}
test bool test4() {
  return isCompiledHygienically(sNames4(0), resolveNames(fix4()));
}


NameGraph sNames5(n) {
  Vs = {x2def};
  Es = ();
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix5() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames5, resolveNames, name2var);
}
test bool test5() {
  return isCompiledHygienically(sNames5(0), resolveNames(fix5()));
}

NameGraph sNames6(n) {
  Vs = {x2def,x2use,x3def,x3use};
  Es = (x3use:x2def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix6() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames6, resolveNames, name2var);
}
test bool test6() {
  return isCompiledHygienically(sNames6(0), resolveNames(fix6()));
}

NameGraph sNames7(n) {
  Vs = {x2use,x3def};
  Es = (x2use:x3def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix7() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames7, resolveNames, name2var);
}
test bool test7() {
  return isCompiledHygienically(sNames7(0), resolveNames(fix7()));
}


NameGraph sNames8(n) {
  Vs = {x2use,x3def,x1def};
  Es = (x2use:x3def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix8() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames8, resolveNames, name2var);
}
test bool test8() {
  return isCompiledHygienically(sNames8(0), resolveNames(fix8()));
}

NameGraph sNames9(n) {
  Vs = {x2use,x3def,x1def,x3use};
  Es = (x2use:x3def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix9() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames9, resolveNames, name2var);
}
test bool test9() {
  return isCompiledHygienically(sNames9(0), resolveNames(fix9()));
}

NameGraph sNames10(n) {
  Vs = {x1use,x3def};
  Es = (x1use:x3def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix10() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames10, resolveNames, name2var);
}
test bool test10() {
  return isCompiledHygienically(sNames10(0), resolveNames(fix10()));
}

// requires three consecutive renamings (recursive calls of fix)
NameGraph sNames11(n) {
  Vs = {x2def,x2use,x3def,x3use};
  Es = (x2use:x2def);
  Ns = ();
  return <Vs,Es,Ns>;
}
Prog fix11() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixHygiene(0, p, sNames11, resolveNames, name2var);
}
test bool test11() {
  return isCompiledHygienically(sNames11(0), resolveNames(fix11()));
}
