module name::Relation

alias NameRel = rel[str name,loc use,loc def];
alias Link = tuple[str,loc,loc];

set[loc] sourceLabels(NameRel sourceNames, NameRel targetNames) =
  sourceNames<1> + sourceNames<2>;
set[loc] targetLabels(NameRel sourceNames, NameRel targetNames) =
  targetNames<1> + targetNames<2>;
set[loc] synthesizedLabels(NameRel sourceNames, NameRel targetNames) =
  targetLabels(sourceNames, targetNames) - sourceLabels(sourceNames, targetNames);

