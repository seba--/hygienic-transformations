module lang::lambda::Test

import name::HygienicCorrectness;

import lang::lambda::Syntax;
import lang::lambda::Names;
import lang::lambda::Semantics;

public Exp testProg = parse("(lambda y. lambda z. y + z) z 3");

tuple[Exp,Exp] renamedGood() {
  app(app(lambda(yvar, lambda(zvar, _)), _), _) = testProg;
  
  renamed1 = rename(testProg, yvar, "foo");
  renamed2 = rename(renamed1, zvar, "bar");
  return <renamed1, renamed2>;
}

test bool testRenamedGood() {
  <renamed1,renamed2> = renamedGood();
  return resolve(testProg) == resolve(renamed1) && resolve(renamed1) == resolve(renamed2);
}

Exp renamedBad() {
  app(app(lambda(yvar, lambda(zvar, _)), _), _) = testProg;
  return rename(testProg, yvar, "z");
}

test bool testRenamedBad() {
  Exp renamed = renamedBad();
  return resolve(testProg) != resolve(renamed);
}
