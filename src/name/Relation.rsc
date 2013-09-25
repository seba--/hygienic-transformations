module name::Relation

alias Name = tuple[str name, loc l];
alias Link = tuple[loc use,loc def];
alias NameRel = set[Link];
alias NameGraph = tuple[set[Name], NameRel];

set[loc] sourceLabels(NameRel sourceNames, NameRel targetNames) =
  sourceNames<1> + sourceNames<2>;
set[loc] targetLabels(NameRel sourceNames, NameRel targetNames) =
  targetNames<1> + targetNames<2>;
set[loc] synthesizedLabels(NameRel sourceNames, NameRel targetNames) =
  targetLabels(sourceNames, targetNames) - sourceLabels(sourceNames, targetNames);

