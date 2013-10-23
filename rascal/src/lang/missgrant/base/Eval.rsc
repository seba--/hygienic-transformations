module lang::missgrant::base::Eval


import lang::missgrant::base::AST;
import lang::missgrant::base::Extract;
import IO;
import Set;

alias Output = tuple[str state, list[str] commands];

/*
 * Example:
 * eval(load(|project://MissGrant/input/missgrant.ctl|), ["D1CL", "D2OP", "L1ON"]);
 * ==> <"unlockedPanel",["PNUL","D1LK"]>
 */
 
TransRel transRelToken(Controller ctl) 
  = {  <s1, tk, s2> | <s1, e, s2> <- transRel(ctl), event(e, tk) <- ctl.events };

ActionRel commandTokens(Controller ctl) 
  = { <s, tk> | <s, a> <- commands(ctl), command(a, tk) <- ctl.commands }; 

Output eval(Controller ctl, list[str] tokens)
  = eval(transRelToken(ctl), commandTokens(ctl), ctl.states[0].name, tokens);

Output eval(TransRel tr, ActionRel as, str init, list[str] input) =
   (<init, []> | compose(it, step(tr, as, it[0], token)) | token <- input);

Output step(TransRel trans, ActionRel commands, str from, str token) {
  if (c <- trans[from,token]) { // choose arb. one if non-det.
    return <c, toList(commands[c])>;
  } 
  return <from, []>;
}

Output compose(Output a, Output b) = <b.state , a.commands + b.commands>;
