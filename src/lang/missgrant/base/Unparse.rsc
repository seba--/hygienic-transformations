module lang::missgrant::base::Unparse

import lang::missgrant::base::AST;
import lang::missgrant::base::Parse;
import lang::missgrant::base::Implode;
import List;
import ParseTree;

test bool unparseMissgrant() = testHelper(|project://MissGrant/input/missgrant.ctl|);
test bool unparseMoreMissgrant() = testHelper(|project://MissGrant/input/moremissgrant.ctl|);
test bool unparseMisterJones() = testHelper(|project://MissGrant/input/misterjones.ctl|);

     
private bool testHelper(loc l) {
  ast = implode(parse(l));
  return implode(parse(unparse(ast), l)) == ast;
}

str unparse(controller(es, rs, cs, ss)) =
  "<unparse(es)>
  '<unparse(rs)>
  '<unparse(cs)>
  '<unparse(ss)>";

str unparse(list[Event] es) =
  "events
  '  <for (e <- es) {>
  '  <unparse(e)>
  '  <}>
  'end";
  
str unparse(list[str] rs) = rs == [] ? "" :  
  "resetEvents
  '  <intercalate("\n", rs)>
  'end";  

str unparse(list[Command] cs) =
  "commands
  '  <for (c <- cs) {>
  '  <unparse(c)>
  '  <}>
  'end";

str unparse(list[State] ss) = intercalate("\n", [ unparse(s) | s <- ss ]);

str unparse(event(n, t)) = "<n> <t>";
str unparse(command(n, t)) = "<n> <t>";
str unparse(state(n, as, ts)) = 
  "state <n>
  '  <if (as != []) {>
  '  actions {<intercalate(" ", as)>}
  '  <}>
  '  <for (t <- ts) {>
  '  <unparse(t)>
  '  <}>
  'end";

str unparse(transition(e, t)) = "<e> =\> <t>";
str unparse(transition(n, e, s)) = "after <n> <e> =\> <s>";
