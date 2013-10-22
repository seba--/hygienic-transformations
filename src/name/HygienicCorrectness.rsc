module name::HygienicCorrectness

import name::Relation;

import IO;

set[&T] delta(set[&T] s1, set[&T] s2) =
  { x | x <- s1, x notin s2 } + { y | y <- s2, y notin s1 };

@doc {
Uses from the source point to the same definition in the target.
@return set of illegal links.
}
set[Link] sourcePreservation(NameGraph sNames, NameGraph tNames) {
  sLabels = sNames[0]<1>;
  // Reason for condition: u != d
  //   A transformation can always duplicate a source name to modularize
  //   the resulting program by splitting the corresponding source-language
  //   concept into a definition and one or more references to this definition.
  return {<u,d> | <u,d> <- tNames[1], u in sLabels, u != d, <u,d> notin sNames[1]};
}


@doc {
Synthesized names never point to source labels.
@return set of illegal links.
}
set[Link] synthesizedNotCaptured(NameGraph sNames, NameGraph tNames) {
  sLabels = sNames[0]<1>;
  return {<u,d> | <u,d> <- tNames[1], u notin sLabels, d in sLabels};
}

set[Link] unhygienicLinks(NameGraph sNames, NameGraph tNames) = 
  sourcePreservation(sNames, tNames) + synthesizedNotCaptured(sNames, tNames);

bool isCompiledHygienically(NameGraph sNames, NameGraph tNames) =
  unhygienicLinks(sNames, tNames) == {};

