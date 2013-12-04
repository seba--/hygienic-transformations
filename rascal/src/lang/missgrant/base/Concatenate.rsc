module lang::missgrant::base::Concatenate

import lang::missgrant::base::AST;
import lang::missgrant::base::Implode;
import lang::missgrant::base::NameRel;
import lang::missgrant::base::Unparse;
import lang::simple::Compile;
import lang::simple::Pretty;
import lang::simple::AST;
import lang::simple::NameRel;
import name::HygienicCorrectness;
import name::Relation;
import name::NameFix;

import IO;
import String;

Controller concatenate(Controller ctl1, Controller ctl2) {
  events = ctl1.events + ctl2.events; // use union to eliminate duplicates?
  resets = ctl1.resets + ctl2.resets;
  commands = ctl1.commands + ctl2.commands;

  states = connect(ctl1.states, ctl2.states);
  
  return controller(events, resets, commands, states);
}

list[State] connect(states1, states2) {
  init2 = states2[0];
  
  result = [ state(s.name, s.actions, s.transitions + connectFinalStates(s.transitions, init2)) | s <- states1 ];
  result += states2;
  return result;
}

list[Transition] connectFinalStates(transitions, init2) =
  [ transition(t.event, init2.name) | t <- transitions, isFinal(t.state) ];

bool isFinal(str state) = endsWith(state, "_final");




bool testConcat(ctl1, ctl2, ctlConcat) {
  G1 = resolveNames(ctl1);
  G2 = resolveNames(ctl2);
  G12 = union(G1, G2);
  
  G = resolveNames(ctlConcat);

  return isCompiledHygienically(G12, G);  
}

Controller missgrant = load(|project://Rascal-Hygiene/input/missgrant-final.ctl|);
Controller misterjones = load(|project://Rascal-Hygiene/input/misterjones.ctl|);
Controller doorsfinal = load(|project://Rascal-Hygiene/input/doors-final.ctl|);
Controller doorsfinal2 = load(|project://Rascal-Hygiene/input/doors-final2.ctl|);

Controller capturingConcat() = concatenate(missgrant, misterjones);
test bool test1() = ! testConcat(missgrant, misterjones, capturingConcat());

Controller fixedCapturingConcat(Controller c1, Controller c2) {
  ctl = concatenate(c1, c2);

  G1 = resolveNames(c1);
  G2 = resolveNames(c2);
  G12 = union(G1, G2);
  
  x = nameFix(#Controller, G12, ctl, resolveNames);
  println(unparse(x));
  
  Prog smpl = nameFix(#Prog, G12, compile(ctl), lang::simple::NameRel::resolveNames);
  println(pretty(smpl));
    
  return x;
}
test bool test2() = testConcat(missgrant, misterjones, 
   fixedCapturingConcat(missgrant, misterjones));

test bool test3() = testConcat(doorsfinal, doorsfinal2, 
    fixedCapturingConcat(doorsfinal, doorsfinal2));
