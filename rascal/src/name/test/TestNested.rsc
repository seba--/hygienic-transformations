module name::TestNested


import lang::missgrant::base::AST;
//import lang::missgrant::base::NameRel;

import lang::simple::AST;
import lang::simple::Compile;
import lang::simple::Implode;
import lang::simple::NameRel;
import lang::simple::Parse;
import lang::simple::Pretty;

import name::NameGraph;
import name::HygienicCorrectness;
import name::VisualizeRelation;
import name::NameFix;
import name::IDs;

import IO;

loc progloc = |project://Rascal-Hygiene/input/testnested.sim|;

str x1def = "x";
str x1use = "x";
str x2def = "x";
str x2use = "x";
str x3def = "x";
str x3use = "x";

str testprog =
  "{var <x1def> = 1; <x1use> + {var <x2def> = 1; <x2use> + {var <x3def> = 1; <x3use>}}}";
Prog theProg =
  prog([],
       [let(x1def, val(nat(1)),
              plus(var(x1use),
                   let(x2def, val(nat(1)),
                         plus(var(x2use),
                              let(x3def, val(nat(1)),
                                    var(x3use))))))]);
Prog prog() = theProg;
NameGraph resolve() = resolveNames(prog());


str testprog2 =
  "{var <x1def> = 1; <x1use>};{var <x2def> = 1; <x2use> + {var <x3def> = 1; <x3use>}}";
Prog theProg2 =
  prog([],
       [sequ(
          let(x1def, val(nat(1)),
                var(x1use)),
          let(x2def, val(nat(1)),
                plus(var(x2use),
                     let(x3def, val(nat(1)),
                           var(x3use)))))]);
Prog prog2() = theProg2;
NameGraph resolve2() = resolveNames(prog2());

Prog fixAndPrint(NameGraph g, Prog p, NameGraph(Prog) resolve) {
  p2 = nameFix(#Prog, g, p, resolve);
  println("fixed: <pretty(p2)>");
  return p2;
}

NameGraph sNames1() {
  Vs = {getID(x3def), getID(x3use)};
  Es = (getID(x3use):getID(x3def));
  return <Vs,Es>;
}
Prog fix1() {
  Prog p = prog();
  NameGraph tNames = resolveNames(p);
  return fixAndPrint(sNames1(), p, resolveNames);
}
test bool test1() {
  return isCompiledHygienically(sNames1(), resolveNames(fix1()));
}

NameGraph sNames2() {
  Vs = {getID(x2def), getID(x3use)};
  Es = (getID(x3use):getID(x2def));
  return <Vs,Es>;
}
Prog fix2() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixAndPrint(sNames2(), p, resolveNames);
}
test bool test2() {
  return isCompiledHygienically(sNames2(), resolveNames(fix2()));
}


NameGraph sNames3() {
  Vs = {getID(x1def), getID(x3use)};
  Es = (getID(x3use):getID(x1def));
  return <Vs,Es>;
}
Prog fix3() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixAndPrint(sNames3(), p, resolveNames);
}
test bool test3() {
  return isCompiledHygienically(sNames3(), resolveNames(fix3()));
}

NameGraph sNames4() {
  Vs = {getID(x1def), getID(x2use)};
  Es = (getID(x2use):getID(x1def));
  return <Vs,Es>;
}
Prog fix4() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixAndPrint(sNames4(), p, resolveNames);
}
test bool test4() {
  return isCompiledHygienically(sNames4(), resolveNames(fix4()));
}


NameGraph sNames5() {
  Vs = {getID(x2def)};
  Es = ();
  return <Vs,Es>;
}
Prog fix5() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixAndPrint(sNames5(), p, resolveNames);
}
test bool test5() {
  return isCompiledHygienically(sNames5(), resolveNames(fix5()));
}

NameGraph sNames6() {
  Vs = {getID(x2def),getID(x2use),getID(x3def),getID(x3use)};
  Es = (getID(x3use):getID(x2def));
  return <Vs,Es>;
}
Prog fix6() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixAndPrint(sNames6(), p, resolveNames);
}
test bool test6() {
  return isCompiledHygienically(sNames6(), resolveNames(fix6()));
}

NameGraph sNames7() {
  Vs = {getID(x2use),getID(x3def)};
  Es = (getID(x2use):getID(x3def));
  return <Vs,Es>;
}
Prog fix7() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixAndPrint(sNames7(), p, resolveNames);
}
test bool test7() {
  return isCompiledHygienically(sNames7(), resolveNames(fix7()));
}


NameGraph sNames8() {
  Vs = {getID(x2use),getID(x3def),getID(x1def)};
  Es = (getID(x2use):getID(x3def));
  return <Vs,Es>;
}
Prog fix8() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixAndPrint(sNames8(), p, resolveNames);
}
test bool test8() {
  return isCompiledHygienically(sNames8(), resolveNames(fix8()));
}

NameGraph sNames9() {
  Vs = {getID(x2use),getID(x3def),getID(x1def),getID(x3use)};
  Es = (getID(x2use):getID(x3def));
  return <Vs,Es>;
}
Prog fix9() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixAndPrint(sNames9(), p, resolveNames);
}
test bool test9() {
  return isCompiledHygienically(sNames9(), resolveNames(fix9()));
}

NameGraph sNames10() {
  Vs = {getID(x1use),getID(x3def)};
  Es = (getID(x1use):getID(x3def));
  return <Vs,Es>;
}
Prog fix10() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixAndPrint(sNames10(), p, resolveNames);
}
test bool test10() {
  return isCompiledHygienically(sNames10(), resolveNames(fix10()));
}

// requires three consecutive renamings (recursive calls of fix)
NameGraph sNames11() {
  Vs = {getID(x2def),getID(x2use),getID(x3def),getID(x3use)};
  Es = (getID(x2use):getID(x2def));
  return <Vs,Es>;
}
Prog fix11() {
  Prog p = prog();
  tNames = resolveNames(p);
  return fixAndPrint(sNames11(), p, resolveNames);
}
test bool test11() {
  return isCompiledHygienically(sNames11(), resolveNames(fix11()));
}


// test 12 illustrates that name-fix may introduce free variable if
// use and definition are in separate scopes. [note: test uses prog2()]
NameGraph sNames12() {
  Vs = {getID(x1def), getID(x3use)};
  Es = (getID(x3use):getID(x1def));
  return <Vs,Es>;
}
Prog fix12() {
  Prog p = prog2();
  tNames = resolveNames(p);
  return fixAndPrint(sNames12(), p, resolveNames);
}
test bool test12() {
  return isCompiledHygienically(sNames12(), resolveNames(fix12()));
}

