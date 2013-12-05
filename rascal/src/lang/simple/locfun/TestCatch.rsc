module lang::simple::locfun::TestCatch

import lang::simple::locfun::Catch;
import lang::simple::locfun::Locfun;
import lang::simple::AST;
import lang::simple::Implode;
import IO;
import lang::simple::NameRel;
import name::NameFix;
import name::HygienicCorrectness;


Prog prog1() = load(|project://Rascal-Hygiene/input/catchit.sim|);

void printProg1() {
  println(pretty(desugar(prog1())));
}


bool testProgFixed1() {
  p1 = prog1();
  G = resolveNames(p1);
  p2 = desugar0(p1);
  
  iprintln(G);
  
  p3 = nameFix(#Prog, G, p2, resolveNames); 
  
  iprintln(resolveNames(p2));

  println(pretty(p2));
  println(pretty(p3));
  return true;
}

bool testProgFixed2() {
  p1 = prog1();
  G = resolveNames(p1);
  p2 = desugar(p1);
  
  iprintln(G);
  
  p3 = nameFix(#Prog, G, p2, resolveNames); 
  
  iprintln(resolveNames(p2));

  println(pretty(p2));
  println(pretty(p3));
  return true;
}