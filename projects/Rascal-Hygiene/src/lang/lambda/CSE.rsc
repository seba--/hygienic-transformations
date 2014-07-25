module lang::lambda::CSE

import lang::lambda::Syntax;
import lang::lambda::Names;
import lang::lambda::Equal;
import name::NameGraph;

import IO;
import List;
import Map;
import Set;
import util::Maybe;
import util::Bag;


bool shouldReplace(plus(_, _)) = true;
bool shouldReplace(app(_, _)) = true;
default bool shouldReplace(e) = false;


Bag[Exp] expressionCounts(Exp e, NameGraph G) {
  subexps = [sub | /Exp sub <- e];
  return makeBag(subexps, bool (Exp e1, Exp e2) {return equal(e1, e2, G);});
}

map[Exp, str] makeNewvars(counts) {
  int newvarCount = 0;
  str makeNewvar() {
    v = "x<newvarCount>";
    newvarCount += 1;
    return v;
  }
  return (rep:makeNewvar() | rep <- counts);
}

anno Bag[str] Exp @ eliminated;

Exp CSE(Exp e) {
  G = resolve(e);
  allcounts = expressionCounts(e, G);
  counts = (rep:count | <rep,count> <- allcounts<0,1>, count >= 2, shouldReplace(rep));
  newvars = makeNewvars(counts);
  
  return bottom-up visit(e) {
    case Exp sub: {
      if ([rep] := [rep | rep <- newvars, equal(rep, sub, G)]) {
        elimVar = newvars[rep];
        insert var(elimVar)[@eliminated=makeBag([elimVar])];
      } else {
        sub = propagateEliminated(sub);
        
        res = sub;
        for (rep <- newvars) {
          elimVar = newvars[rep];
          if(elimVar in sub@eliminated && sub@eliminated[elimVar] == counts[rep]) {
            res = app(lambda(elimVar, res), rep);
            res@eliminated = delete(sub@eliminated, elimVar);
          }
        }
        insert res;
      }
    }
  };
}

Exp propagateEliminated(Exp sub) {
  thisElimCount = ();
  for (Exp subsub <- sub)
    thisElimCount = bagAddAll(thisElimCount, subsub@eliminated);
  sub@eliminated = thisElimCount;
  return sub;
}