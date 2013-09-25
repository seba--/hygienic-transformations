module name::Relation

alias Name = tuple[str name, loc l];
alias Link = tuple[loc use,loc def];
alias NameRel = set[Link];
alias NameGraph = tuple[set[Name], NameRel];

set[loc] sourceLabels(NameGraph sourceNames, NameGraph targetNames) =
  sourceNames[0]<1>;
set[loc] targetLabels(NameGraph sourceNames, NameGraph targetNames) =
  targetNames[0]<1>;
set[loc] synthesizedLabels(NameGraph sourceNames, NameGraph targetNames) =
  targetLabels(sourceNames, targetNames) - sourceLabels(sourceNames, targetNames);

