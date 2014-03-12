module name::Gensym

import name::NameGraph;

str gensym(str base, set[str] used) = gensym(base, used, 0);

str gensym(str base, set[str] used, int n) {
  name = "<base>_<n>";
  if (name notin used)
    return name;
  return gensym(base, used, n+1);
}

test bool nameIsFresh(str base) = gensym(base, {base,"<base>_0"}) notin {base};
test bool nameIsFresh(str base, set[str] used) = gensym(base, used) notin used;
