module name::HygienicCorrectness

import name::Relation;

import IO;

set[&T] delta(set[&T] s1, set[&T] s2) =
  { x | x <- s1, x notin s2 } + { y | y <- s2, y notin s1 };

@doc {
Uses from the source point to the same definition in the target.
@return set of illegal links.
}
set[Link] sourcePreservation(NameRel sNames, NameRel tNames) {
  sUses = sNames<1>;
  sMap = sNames<1,2>;
  return {<n,u,d> | <n,u,d> <- tNames, u in sUses, d notin sMap[u]};
}


@doc {
Synthesized names never point to source labels.
@return set of illegal links.
}
set[Link] synthesizedNotCaptured(NameRel sNames, NameRel tNames) {
  sLabels = sourceLabels(sNames, tNames);
  
  return {<n,u,d> | <n,u,d> <- tNames, u notin sLabels, d in sLabels};
}

set[Link] unhygienicLinks(NameRel sNames, NameRel tNames) = 
  sourcePreservation(sNames, tNames) + synthesizedNotCaptured(sNames, tNames);

bool isCompiledHygienically(NameRel sNames, NameRel tNames) =
  unhygienicLinks(sNames, tNames) == {};

