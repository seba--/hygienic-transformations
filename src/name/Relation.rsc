module name::Relation

alias NameRel = rel[str,loc,loc];
alias Link = tuple[loc,loc];

set[loc] sourceLabels(rel[str, loc, loc] sourceNames, rel[str, loc, loc] targetNames) =
  sourceNames<1> + sourceNames<2>;
set[loc] targetLabels(rel[str, loc, loc] sourceNames, rel[str, loc, loc] targetNames) =
  targetNames<1> + targetNames<2>;
set[loc] synthesizedLabels(rel[str, loc, loc] sourceNames, rel[str, loc, loc] targetNames) =
  targetLabels(sourceNames, targetNames) - sourceLabels(sourceNames, targetNames);

