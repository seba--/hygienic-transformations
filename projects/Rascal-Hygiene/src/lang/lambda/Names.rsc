module lang::lambda::Names

import lang::lambda::Syntax;

import name::NameGraph;
import name::IDs;
import name::Rename;

alias Scope = map[str, ID];

NameGraph resolve(Exp e) = resolve(e, ());

NameGraph resolve(var(v), scope) {
  id = getID(v);
  if (v in scope) return <{id}, (id:scope[v])>;
  else return <{id}, ()>;
}
NameGraph resolve(lambda(v, body), scope) {
  id = getID(v);
  <V,E> = resolve(body, scope + (v:id));
  return <V + {id}, E>;
}

default NameGraph resolve(Exp e, scope) {
  <V,E> = <{},()>;
  for (Exp subexp <- e) {
    <SubV,SubE> = resolve(subexp, scope);
    <V,E> = <V + SubV,E + SubE>;
  }
  return <V,E>;
}


Exp rename(Exp e1, str oldname, str newname) = rename(e1, resolve(e1), oldname, newname);
