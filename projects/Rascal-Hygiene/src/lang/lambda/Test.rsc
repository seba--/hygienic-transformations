module lang::lambda::Test

import name::HygienicCorrectness;

import lang::lambda::Syntax;
import lang::lambda::Names;
import lang::lambda::Semantics;

public Exp testProg1 = parse("(lambda y. lambda z. y + z) x 3");
public Exp testProg2 = parse("(lambda y. lambda z. y + z) y 3");
public Exp testProg3 = parse("(lambda y. lambda z. y + z) z 3");

test bool testNormalize1() = plus(var(_), nat(3)) := norm(testProg1);
test bool testNormalize2() = plus(var(_), nat(3)) := norm(testProg2);
test bool testNormalize3() = plus(var(_), nat(3)) := norm(testProg3);



tuple[Exp,Exp] renamedGood(prog) {
  app(app(lambda(yvar, lambda(zvar, _)), _), _) = prog;
  
  renamed1 = rename(prog, yvar, "foo");
  renamed2 = rename(renamed1, zvar, "bar");
  return <renamed1, renamed2>;
}
bool testRenamedGood(prog) {
  <renamed1,renamed2> = renamedGood(prog);
  return resolve(prog) == resolve(renamed1) && resolve(renamed1) == resolve(renamed2);
}
test bool testRenamedGood1() = testRenamedGood(testProg1);
test bool testRenamedGood2() = testRenamedGood(testProg2);
test bool testRenamedGood3() = testRenamedGood(testProg3);

Exp renamedBad(prog) {
  app(app(lambda(yvar, lambda(zvar, _)), _), _) = prog;
  return rename(prog, yvar, "z");
}
bool testRenamedBad(prog) {
  Exp renamed = renamedBad(prog);
  return resolve(prog) != resolve(renamed);
}
test bool testRenamedGood1() = testRenamedBad(testProg1);
test bool testRenamedGood2() = testRenamedBad(testProg2);
test bool testRenamedGood3() = testRenamedBad(testProg3);
