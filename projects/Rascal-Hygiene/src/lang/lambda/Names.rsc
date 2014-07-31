module lang::lambda::Names

import lang::lambda::Syntax;

import name::NameGraph;
import name::IDs;
import name::Rename;

alias Scope = map[str, ID];

NameGraph resolve(Exp e) = resolve(e, ());

NameGraph resolve(nat(n), scope) = <{},()>;
NameGraph resolve(var(v), scope) {
  id = getID(v);
  if (v in scope) return <{id}, (id:scope[v])>;
  else return <{id}, ()>; // or throw "Unbound variable <v>";
}
NameGraph resolve(lambda(v, body), scope) {
  id = getID(v);
  <V,E> = resolve(body, scope + (v:id));
  return <V + {id}, E>;
}
NameGraph resolve(plus(e1, e2), scope) {
  <V1,E1> = resolve(e1, scope);
  <V2,E2> = resolve(e2, scope);
  return <V1+V2,E1+E2>;
}
NameGraph resolve(app(e1, e2), scope) {
  <V1,E1> = resolve(e1, scope);
  <V2,E2> = resolve(e2, scope);
  return <V1+V2,E1+E2>;
}


Exp rename(Exp e1, str oldname, str newname) = rename(e1, resolve(e1), oldname, newname);

set[str] freevars(Exp e) = freevars(e, resolve(e));
set[str] freevars(Exp e, G) = {ref | /var(ref) <- e, isFree(ref, G)};
