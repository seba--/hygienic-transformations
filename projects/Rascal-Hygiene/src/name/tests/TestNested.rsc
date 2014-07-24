module name::tests::TestNested


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
import name::figure::VisualizeRelation;
import name::NameFix;
import name::IDs;

import IO;

private loc progloc = |project://Rascal-Hygiene/input/testnested.sim|;

private str x1def = "x";
private str x1use = "x";
private str x2def = "x";
private str x2use = "x";
private str x3def = "x";
private str x3use = "x";

private str testprog =
  "{var <x1def> = 1; <x1use> + {var <x2def> = 1; <x2use> + {var <x3def> = 1; <x3use>}}}";
Prog theProg =
  prog([],
       [let(x1def, val(nat(1)),
              plus(var(x1use),
                   let(x2def, val(nat(1)),
                         plus(var(x2use),
                              let(x3def, val(nat(1)),
                                    var(x3use))))))]);
private Prog testProg() = theProg;
private NameGraph resolve() = resolveNames(testProg());


private str testprog2 =
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
private Prog testProg2() = theProg2;
private NameGraph resolve2() = resolveNames(testProg2());

private Prog fixAndPrint(NameGraph g, Prog p, NameGraph(Prog) resolve) {
  p2 = nameFix(#Prog, g, p, resolve);
  println("fixed: <pretty(p2)>");
  return p2;
}

private NameGraph sNames1() {
  Vs = {getID(x3def), getID(x3use)};
  Es = (getID(x3use):getID(x3def));
  return <Vs,Es>;
}
private Prog fix1() {
  Prog p = testProg();
  NameGraph tNames = resolveNames(p);
  return fixAndPrint(sNames1(), p, resolveNames);
}
test bool testNest1() {
  return isHygienic(sNames1(), resolveNames(fix1()));
}

private NameGraph sNames2() {
  Vs = {getID(x2def), getID(x3use)};
  Es = (getID(x3use):getID(x2def));
  return <Vs,Es>;
}
private Prog fix2() {
  Prog p = testProg();
  tNames = resolveNames(p);
  return fixAndPrint(sNames2(), p, resolveNames);
}
test bool testNest2() {
  return isHygienic(sNames2(), resolveNames(fix2()));
}


private NameGraph sNames3() {
  Vs = {getID(x1def), getID(x3use)};
  Es = (getID(x3use):getID(x1def));
  return <Vs,Es>;
}
private Prog fix3() {
  Prog p = testProg();
  tNames = resolveNames(p);
  return fixAndPrint(sNames3(), p, resolveNames);
}
test bool testNest3() {
  return isHygienic(sNames3(), resolveNames(fix3()));
}

private NameGraph sNames4() {
  Vs = {getID(x1def), getID(x2use)};
  Es = (getID(x2use):getID(x1def));
  return <Vs,Es>;
}
private Prog fix4() {
  Prog p = testProg();
  tNames = resolveNames(p);
  return fixAndPrint(sNames4(), p, resolveNames);
}
test bool testNest4() {
  return isHygienic(sNames4(), resolveNames(fix4()));
}


private NameGraph sNames5() {
  Vs = {getID(x2def)};
  Es = ();
  return <Vs,Es>;
}
private Prog fix5() {
  Prog p = testProg();
  tNames = resolveNames(p);
  return fixAndPrint(sNames5(), p, resolveNames);
}
test bool testNest5() {
  return isHygienic(sNames5(), resolveNames(fix5()));
}

private NameGraph sNames6() {
  Vs = {getID(x2def),getID(x2use),getID(x3def),getID(x3use)};
  Es = (getID(x3use):getID(x2def));
  return <Vs,Es>;
}
private Prog fix6() {
  Prog p = testProg();
  tNames = resolveNames(p);
  return fixAndPrint(sNames6(), p, resolveNames);
}
test bool testNest6() {
  return isHygienic(sNames6(), resolveNames(fix6()));
}

private NameGraph sNames7() {
  Vs = {getID(x2use),getID(x3def)};
  Es = (getID(x2use):getID(x3def));
  return <Vs,Es>;
}
private Prog fix7() {
  Prog p = testProg();
  tNames = resolveNames(p);
  return fixAndPrint(sNames7(), p, resolveNames);
}
test bool testNest7() {
  return isHygienic(sNames7(), resolveNames(fix7()));
}


private NameGraph sNames8() {
  Vs = {getID(x2use),getID(x3def),getID(x1def)};
  Es = (getID(x2use):getID(x3def));
  return <Vs,Es>;
}
private Prog fix8() {
  Prog p = testProg();
  tNames = resolveNames(p);
  return fixAndPrint(sNames8(), p, resolveNames);
}
test bool testNest8() {
  return isHygienic(sNames8(), resolveNames(fix8()));
}

private NameGraph sNames9() {
  Vs = {getID(x2use),getID(x3def),getID(x1def),getID(x3use)};
  Es = (getID(x2use):getID(x3def));
  return <Vs,Es>;
}
private Prog fix9() {
  Prog p = testProg();
  tNames = resolveNames(p);
  return fixAndPrint(sNames9(), p, resolveNames);
}
test bool testNest9() {
  return isHygienic(sNames9(), resolveNames(fix9()));
}

private NameGraph sNames10() {
  Vs = {getID(x1use),getID(x3def)};
  Es = (getID(x1use):getID(x3def));
  return <Vs,Es>;
}
private Prog fix10() {
  Prog p = testProg();
  tNames = resolveNames(p);
  return fixAndPrint(sNames10(), p, resolveNames);
}
test bool testNest10() {
  return isHygienic(sNames10(), resolveNames(fix10()));
}

// requires three consecutive renamings (recursive calls of fix)
private NameGraph sNames11() {
  Vs = {getID(x2def),getID(x2use),getID(x3def),getID(x3use)};
  Es = (getID(x2use):getID(x2def));
  return <Vs,Es>;
}
private Prog fix11() {
  Prog p = testProg();
  tNames = resolveNames(p);
  return fixAndPrint(sNames11(), p, resolveNames);
}
test bool testNest11() {
  return isHygienic(sNames11(), resolveNames(fix11()));
}


// test 12 illustrates that name-fix may introduce free variable if
// use and definition are in separate scopes. [note: test uses testProg2()]
private NameGraph sNames12() {
  Vs = {getID(x1def), getID(x3use)};
  Es = (getID(x3use):getID(x1def));
  return <Vs,Es>;
}
private Prog fix12() {
  Prog p = testProg2();
  tNames = resolveNames(p);
  return fixAndPrint(sNames12(), p, resolveNames);
}
test bool testNest12() {
  return isHygienic(sNames12(), resolveNames(fix12()));
}

