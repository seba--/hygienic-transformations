module lang::simple::aif::AIf

extend lang::simple::Syntax;
extend lang::simple::AST;
extend lang::simple::NameRel;
extend lang::simple::Pretty;

import name::IDs;
import name::Relation;

import IO;
import Set;
import List;
import String;


Prog desugarOrThenAIf(Prog p) = 
  visit (p) { case Exp e => desugarAIf(desugarOr(e)) };

str pretty(aif(Exp c, Exp t, Exp e)) = "aif <pretty(c)> then <pretty(t)> else <pretty(e)>";

Prog desugarAIf0(Prog p) = visit (p) { case Exp e => desugarAIf0(e) };

Exp desugarAIf0(aif(c, t, e)) =
  let("it", c, cond(var("it"), t, e));

Exp desugarAIf0(or(e1, e2)) 
  = desugarAIf0(aif(e1, var("it"), e2));
  
default Exp desugarAIf0(Exp e) = e;

Prog desugarAIf(Prog p) = visit (p) { case Exp e => desugarAIf(e) };
  
Exp desugarAIf(aif(c, t, e)) =
  let("it", c, cond(var("it"), 
     allowCapture("it", t), allowCapture("it", e)));

default Exp desugarAIf(Exp e) = e;

Prog desugarOr(Prog p) = visit (p) { case Exp e => desugarOr(e) };

Exp desugarOr(or(e1, e2)) 
  = aif(e1, var("it"), e2);
  
default Exp desugarOr(Exp e) = e;


&T allowCapture(str name, &T t) {
  return visit (t) {
    case str name2 => tagString(name2, "synth", "true")
      when name2 == name
  }
}