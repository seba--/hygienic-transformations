module name::TestEquivalence

import lang::simple::AST;
import lang::simple::NameRel;

import name::Names;
import name::Equivalence;
import name::Relation;

import IO;

loc progloc = |project://Rascal-Hygiene/input/testnested.sim|;

str x1def = "x";
str x1use = "x";
str x2def = "x";
str x2use = "x";
str y2def = "y";
str y2use = "y";
str x3def = "x";
str x3use = "x";

public Prog theProg1 =
  prog([],
       [block([vdef(x1def, val(nat(1)))],
              plus(var(x1use),
                   block([vdef(x2def, val(nat(1)))],
                         plus(var(x2use),
                              block([vdef(x3def, val(nat(1)))],
                                    var(x3use))))))]);
NameGraph resolve1() = resolveNames(theProg1);

public Prog theProg2 =
  prog([],
       [block([vdef(x1def, val(nat(1)))],
              plus(var(x1use),
                   block([vdef(setID(y2def,getID(x2def)), val(nat(1)))],
                         plus(var(setID(y2use,getID(x2use))),
                              block([vdef(x3def, val(nat(1)))],
                                    var(x3use))))))]);
NameGraph resolve2() = resolveNames(theProg2);

public Prog theProg3 =
  prog([],
       [block([vdef(x1def, val(nat(1)))],
              block([vdef(x2def, val(nat(1)))],
                    plus(var(x1use), 
                         var(x2use))))]);
NameGraph resolve3() = resolveNames(theProg3);

public Prog theProg4 =
  prog([],
       [block([vdef(x1def, val(nat(1)))],
              block([vdef(setID(y2def,getID(x2def)), val(nat(1)))],
                    plus(var(x1use), 
                         var(setID(y2use,getID(x2use))))))]);
NameGraph resolve4() = resolveNames(theProg4);


test bool testLabel1() {
  return labelEquivalent(theProg1, theProg1);
}
test bool testLabel2() {
  return labelEquivalent(theProg2, theProg2);
}
test bool testLabel3() {
  return labelEquivalent(theProg1, theProg2);
}
test bool testLabel4() {
  return !labelEquivalent(theProg1, prog([],[]));
}
test bool testLabel5() {
  return !labelEquivalent(theProg1, theProg3);
}
test bool testLabel6() {
  return labelEquivalent(theProg3, theProg4);
}


test bool testAlpha1() {
  return alphaEquivalent(theProg1, resolve1(), theProg1, resolve1());
}
test bool testAlpha2() {
  return alphaEquivalent(theProg2, resolve2(), theProg2, resolve2());
}
test bool testAlpha3() {
  return alphaEquivalent(theProg1, resolve1(), theProg2, resolve2());
}
test bool testAlpha3() {
  return !alphaEquivalent(theProg1, resolve1(), theProg3, resolve3());
}
test bool testAlpha4() {
  return !alphaEquivalent(theProg3, resolve3(), theProg4, resolve4());
}

