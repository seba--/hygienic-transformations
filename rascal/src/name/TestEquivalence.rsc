module name::TestEquivalence

import lang::simple::AST;
import lang::simple::NameRel;
import lang::simple::Pretty;

import name::Names;
import name::Equivalence;
import name::Relation;
import name::Rename;

import IO;

loc progloc = |project://Rascal-Hygiene/input/testnested.sim|;

public ID xdefID = getID("");
public ID xuse1ID = getID("");
public ID xuse2ID = getID("");
public ID ydefID = getID("");
public ID yuseID = getID("");

public NameGraph G = <{xdefID, xuse1ID, xuse2ID}, (xuse1ID:xdefID, xuse2ID:xdefID), ()>;

Prog mkExample(str xdef, str xuse1, str xuse2, str ydef, str yuse) =
  prog([],
       [block([vdef(setID(xdef, xdefID), val(nat(1)))],
              plus(block([vdef(setID(ydef, ydefID), val(nat(2)))],
                         plus(var(setID(xuse2, xuse2ID)),
                              var(setID(yuse, yuseID)))),
                   var(setID(xuse1, xuse1ID))))]);

bool testSubAlphaEquivalence(p1, p2) {
  bool sub = subAlphaEquivalent(p1, p2, G);
  if (!sub)
    return false;
  
  fix1 = fixHygiene(#Prog, G, p1, resolveNames);
  fix2 = fixHygiene(#Prog, G, p2, resolveNames);
  bool alphaFix = alphaEquivalent(fix1, resolveNames(fix1), fix2, resolveNames(fix2));
  if (!alphaFix)
    println("sub-alpha but not alpha fix:\n<p1>\n<p2>");
  return alphaFix;
}

bool testIsAlphaEquivalentFix(p1, p2) {
  fix1 = fixHygiene(#Prog, G, p1, resolveNames);
  fix2 = fixHygiene(#Prog, G, p2, resolveNames);
  iprintln("fix1: <pretty(fix1)>");
  iprintln("fix2: <pretty(fix2)>");
  return alphaEquivalent(fix1, resolveNames(fix1), fix2, resolveNames(fix2));
}

Prog prog1() = mkExample("x","x","x","y","y");
Prog prog2() = mkExample("z","z","z","y","y");
Prog prog3() = mkExample("x","x","x","z","z");
Prog prog4() = mkExample("x","x","x","x","x");

Prog prog5() = mkExample("x","x","x1","y","y");
Prog prog6() = mkExample("x","x","x","y","y1");
Prog prog7() = mkExample("x","x","x","x","y1");

test bool test_1_2() = testSubAlphaEquivalence(prog1(), prog2());
test bool test_2_3() = testSubAlphaEquivalence(prog2(), prog3());
test bool test_3_4() = testSubAlphaEquivalence(prog3(), prog4());
test bool test_1_5() = !testSubAlphaEquivalence(prog1(), prog5());
test bool test_1_6() = !testSubAlphaEquivalence(prog1(), prog6());
test bool test_1_7() = !testSubAlphaEquivalence(prog1(), prog7());
test bool test_6_7() = testSubAlphaEquivalence(prog6(), prog7());

test bool test_1_2() = testIsAlphaEquivalentFix(prog1(), prog2());
test bool test_2_3() = testIsAlphaEquivalentFix(prog2(), prog3());
test bool test_3_4() = testIsAlphaEquivalentFix(prog3(), prog4());
test bool test_1_5() = !testIsAlphaEquivalentFix(prog1(), prog5());
test bool test_1_6() = !testIsAlphaEquivalentFix(prog1(), prog6());
test bool test_1_7() = !testIsAlphaEquivalentFix(prog1(), prog7());
test bool test_6_7() = testIsAlphaEquivalentFix(prog6(), prog7());


