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
  writeFile(progloc, "{x=1; x + {x=1; x + {x=1; x}}}");
  return load(progloc);
}

NameGraph resolve() = resolveNames(prog());

NameGraph sNames1(n) {
  Vs = {x3def, x3use};
  Es = (x3use:x3def);
  Ns = ();
  return <Vs,Es,Ns>;
}
str fix1() {
  Prog p = prog();
  tNames = resolveNames(p);
  p2 = fixHygiene(0, p, sNames1, resolveNames, name2var);
  assert isCompiledHygienically(sNames1(0), resolveNames(p2)) : "unhygienic links: <unhygienicLinks(sNames1(0), resolveNames(p2))>";
  return pretty(p2);
}

NameGraph sNames2(n) {
  Vs = {x2def, x3use};
  Es = (x3use:x2def);
  Ns = ();
  return <Vs,Es,Ns>;
}
str fix2() {
  Prog p = prog();
  tNames = resolveNames(p);
  p2 = fixHygiene(0, p, sNames2, resolveNames, name2var);
  assert isCompiledHygienically(sNames2(0), resolveNames(p2)) : "unhygienic links: <unhygienicLinks(sNames2(0), resolveNames(p2))>";
  return pretty(p2);
}

NameGraph sNames3(n) {
  Vs = {x1def, x3use};
  Es = (x3use:x1def);
  Ns = ();
  return <Vs,Es,Ns>;
}
str fix3() {
  Prog p = prog();
  tNames = resolveNames(p);
  p2 = fixHygiene(0, p, sNames3, resolveNames, name2var);
  assert isCompiledHygienically(sNames3(0), resolveNames(p2)) : "unhygienic links: <unhygienicLinks(sNames3(0), resolveNames(p2))>";
  return pretty(p2);
}

NameGraph sNames4(n) {
  Vs = {x1def, x2use};
  Es = (x2use:x1def);
  Ns = ();
  return <Vs,Es,Ns>;
}
str fix4() {
  Prog p = prog();
  tNames = resolveNames(p);
  p2 = fixHygiene(0, p, sNames4, resolveNames, name2var);
  assert isCompiledHygienically(sNames4(0), resolveNames(p2)) : "unhygienic links: <unhygienicLinks(sNames4(0), resolveNames(p2))>";
  return pretty(p2);
}

NameGraph sNames5(n) {
  Vs = {x2def};
  Es = ();
  Ns = ();
  return <Vs,Es,Ns>;
}
str fix5() {
  Prog p = prog();
  tNames = resolveNames(p);
  p2 = fixHygiene(0, p, sNames5, resolveNames, name2var);
  assert isCompiledHygienically(sNames5(0), resolveNames(p2)) : "unhygienic links: <unhygienicLinks(sNames5(0), resolveNames(p2))>";
  return pretty(p2);
}

NameGraph sNames6(n) {
  Vs = {x2def,x2use,x3def,x3use};
  Es = (x3use:x2def);
  Ns = ();
  return <Vs,Es,Ns>;
}
str fix6() {
  Prog p = prog();
  tNames = resolveNames(p);
  p2 = fixHygiene(0, p, sNames6, resolveNames, name2var);
  assert isCompiledHygienically(sNames6(0), resolveNames(p2)) : "unhygienic links: <unhygienicLinks(sNames6(0), resolveNames(p2))>";
  return pretty(p2);
}

NameGraph sNames7(n) {
  Vs = {x2use,x3def};
  Es = (x2use:x3def);
  Ns = ();
  return <Vs,Es,Ns>;
}
str fix7() {
  Prog p = prog();
  tNames = resolveNames(p);
  p2 = fixHygiene(0, p, sNames7, resolveNames, name2var);
  assert isCompiledHygienically(sNames7(0), resolveNames(p2)) : "unhygienic links: <unhygienicLinks(sNames7(0), resolveNames(p2))>";
  assert false : "x3use should be bound by x2def";
  return pretty(p2);
}
