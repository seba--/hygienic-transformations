module lang::simple::NameCorrectness

import lang::missgrant::base::AST;
import lang::missgrant::base::NameRel;

import lang::simple::AST;
import lang::simple::NameRel;

alias NameRel = rel[str,loc,loc];

set[loc] sourceLabels(rel[str, loc, loc] sourceNames, rel[str, loc, loc] targetNames) =
  sourceNames<1> + sourceNames<2>;
set[loc] targetLabels(rel[str, loc, loc] sourceNames, rel[str, loc, loc] targetNames) =
  targetNames<1> + targetNames<2>;
set[loc] synthesizedLabels(rel[str, loc, loc] sourceNames, rel[str, loc, loc] targetNames) =
  targetLabels(sourceNames, targetNames) - sourceLabels(sourceNames, targetNames);


@doc{ 
Checks whether the compilation of `ctl` to `p` was hygienic.
}   

@doc {
Uses from the source point to the same definition in the target.
}
bool check1(NameRel sNames, NameRel tNames) {
  set[loc] defOf(NameRel names, loc l) = names<1,2>[l];
  
  bool check(loc u) = u in tNames<1> ? defOf(sNames, u) == defOf(tNames, u) : true;
  
  return ( true | it && check(u) | u <- sNames<1>);
}


@doc {
Synthesized names never point to source labels.
}
bool check2(NameRel sourceNames, NameRel targetNames) {
  targetMap = targetNames<1,2>;
  synDefsT = ( {} | it + targetMap[use]  | use <- targetNames<1>, use notin sourceLabels(sourceNames, targetNames) );
  
  return sourceLabels(sourceNames, targetNames) & synDefsT == {}; 
}


bool isCompiledHygienic(Controller ctl, Prog p) {
  sNames = resolveNames(ctl);
  tNames = resolveNames(p);
  return check1(sNames, tNames) && check2(sNames, tNames);
}
