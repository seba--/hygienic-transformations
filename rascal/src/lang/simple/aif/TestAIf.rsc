module lang::simple::aif::TestAIf

extend lang::simple::aif::AIf;
import lang::simple::Implode;
import name::NameFix;

import ParseTree;
import Node;

Prog prog1() = load(|project://Rascal-Hygiene/input/aif1.sim|);

Prog prog2() = load(|project://Rascal-Hygiene/input/aif2.sim|);


test bool testProgAIfFixed1() {
  p1 = prog1();
  G = resolveNames(p1);
  p2 = desugarAIf0(p1);
  
  p3 = nameFix(#Prog, G, p2, resolveNames); 
  
  println(pretty(p2));
  println(pretty(p3));
  return p2 != p3;
}

test bool testProgAIfFixed2() {
  p1 = prog1();
  G = resolveNames(p1);
  p2 = desugarAIf(p1);
  
  p3 = nameFix(#Prog, G, p2, resolveNames); 
  
  println(pretty(p2));
  println(pretty(p3));
  return p2 == p3;
}


test bool testProgAIfFixed3() {
  p1 = prog2();
  G = resolveNames(p1);
  p2 = desugarAIf0(p1);
  
  p3 = nameFix(#Prog, G, p2, resolveNames); 
  
  println(pretty(p2));
  println(pretty(p3));
  return p2 != p3;
}

test bool testProgAIfFixed4() {
  p1 = prog2();
  G = resolveNames(p1);
  p2 = desugarAIf(p1);
  
  p3 = nameFix(#Prog, G, p2, resolveNames); 
  
  println(pretty(p2));
  println(pretty(p3));
  return p2 == p3;
}