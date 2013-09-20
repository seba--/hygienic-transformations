module name::HygienicCorrectness

import name::Relation;

@doc{ 
Checks whether the compilation of `ctl` to `p` was hygienic.
}   

@doc {
Uses from the source point to the same definition in the target.
}
bool sourcePreservation(NameRel sNames, NameRel tNames) {
  set[loc] defOf(NameRel names, loc l) = names<1,2>[l];
  
  bool check(loc u) = u in tNames<1> ? defOf(sNames, u) == defOf(tNames, u) : true;
  
  return ( true | it && check(u) | u <- sNames<1>);
}


@doc {
Synthesized names never point to source labels.
}
bool synthesizedNotCaptured(NameRel sourceNames, NameRel targetNames) {
  targetMap = targetNames<1,2>;
  synDefsT = ( {} | it + targetMap[use]  | use <- targetNames<1>, use notin sourceLabels(sourceNames, targetNames) );
  
  return sourceLabels(sourceNames, targetNames) & synDefsT == {}; 
}


bool isCompiledHygienically(NameRel (&S) resolveSource, &S sourceProg,
                            NameRel (&T) resolveTarget, &T targetProg) {
  sNames = resolveSource(sourceProg);
  tNames = resolveTarget(targetProg);
  return isCompiledHygienically(sNames, tNames);
}

bool isCompiledHygienically(NameRel sNames, NameRel tNames) =
  sourcePreservation(sNames, tNames) && synthesizedNotCaptured(sNames, tNames);

