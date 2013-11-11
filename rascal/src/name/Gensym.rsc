module name::Gensym

import name::Relation;

tuple[str name, str suffix] freshSuffix(set[str] used, str base) = freshSuffix(used, base, 0);

str suffix(int n) = "_<n>";

tuple[str name, str suffix] freshSuffix(set[str] used, str base, int n) {
  name = base + suffix(n);
  if (name notin used)
    return <name, suffix(n)>;
  return freshName(used, base, n+1);
}

str freshName(set[str] used, str base) = freshName(used, base, 0);

str freshName(set[str] used, str base, int n) {
  name = "<base>_<n>";
  if (name notin used)
    return name;
  return freshName(used, base, n+1);
}

test bool nameIsFresh(str base) = freshName({base,"<base>_0"}, base) notin {base};
test bool nameIsFresh(set[str] used, str base) = freshName(used, base) notin used;
