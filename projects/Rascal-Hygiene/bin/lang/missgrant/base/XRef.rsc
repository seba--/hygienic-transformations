module lang::missgrant::base::XRef

import lang::missgrant::base::Syntax;
import ParseTree;

//start[Controller] xrefController(start[Controller] pt) {
start[Controller] xrefController(Tree pt) {
  stateOrgs = ();
  eventOrgs = ();
  eventTokens = ();  
  commandOrgs = ();
  commandTokens = ();
  
  visit (pt) {
    case lang::missgrant::base::Syntax::State x: stateOrgs[x.name] = x@\loc;
    case lang::missgrant::base::Syntax::Event x: {
      eventOrgs[x.name] = x@\loc;
      eventTokens[x.name] = x.token;
    }
    case lang::missgrant::base::Syntax::Command x: {
      commandOrgs[x.name] = x@\loc;
      commandTokens[x.name] = x.token;
    }
  }
  
  // transition: Id event "=\>" Id state;
  pt = top-down-break visit (pt) {
    case lang::missgrant::base::Syntax::Transition x: {
      if (x.event in eventOrgs) {
        x = x[event=(x.event)[@link=eventOrgs[x.event]]];
        x = x[event=(x.event)[@doc="Token: <eventTokens[x.event]>"]];
      }
      if (x.state in stateOrgs)
        x = x[state=(x.state)[@link=stateOrgs[x.state]]];
      insert x;
    }
    case lang::missgrant::base::Syntax::Actions a: {
      a = visit (a) {
        case lang::missgrant::base::Syntax::Id x: { 
          if (x in commandOrgs) {
           x = x[@link=commandOrgs[x]];
           x = x[@doc="Token: <commandTokens[x]>"];
          }
          insert x;
        }
      }
      insert a; 
    }
  }
  return pt;
}
