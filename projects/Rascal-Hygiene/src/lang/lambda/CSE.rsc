module lang::lambda::CSE

import name::IDs;
import name::NameGraph;
import lang::lambda::Syntax;
import lang::lambda::Names;

import IO;
import List;
import Map;
import util::Maybe;

bool equal(Exp e1, Exp e2) {
  <V1,E1> = resolve(e1);
  <V2,E2> = resolve(e2);
  G = <V1+V2,E1+E2>;
  return equal(e1, e2, G);
}

bool equal(var(v1), var(v2), NameGraph G) {
  id1 = getID(v1);
  id2 = getID(v2);
  return id1 notin G.E && id2 notin G.E || id1 in G.E && id2 in G.E && G.E[id1] == G.E[id2];
}

bool equal(nat(n), nat(n), G) = true;

bool equal(plus(e11, e12), plus(e21, e22), G) =
  equal(e11, e21, G) && equal(e12, e22, G);

bool equal(app(e11, e12), app(e21, e22), G) =
  equal(e11, e21, G) && equal(e12, e22, G);

bool equal(lambda(v1, e1), lambda(v2, e2), NameGraph G) {
  id1 = getID(v1);
  id2 = getID(v2);
  E2 = (ref:id1 | <ref, dec> <- G.E<0,1>, dec == id2);
  G2 = <G.V, G.E + E2>;
  return equal(e1, e2, G2);
}

default bool equal(e1, e2, G) = false;

map[Exp, int] expressionCounts(Exp e) = expressionCounts(e, resolve(e));
map[Exp, int] expressionCounts(Exp e, NameGraph G) {
  counts = ();
  for (/Exp sub <- e) {
    foundRep = [rep | rep <- counts, equal(rep, sub, G)];
    if ([rep] := foundRep)
      counts += (rep:counts[rep]+1);
    else
      counts += (sub:1);
  }
  return counts;
}

alias RepExp = Exp;

Exp CSE(Exp e) {
  G = resolve(e);
  allcounts = expressionCounts(e, G);
  counts = (rep:count | <rep,count> <- allcounts<0,1>, count >= 2, shouldReplace(rep));
  
  map[RepExp, str] newvars = ();
  map[Exp, list[str]] eliminatedVars = ();

  return bottom-up visit(e) {
    case Exp sub: {
      if (just(<elimVar,newvarsNew>) := eliminateCommon(sub, newvars, counts, G)) {
        newvars = newvarsNew;
        res = var(elimVar);
        eliminatedVars += (res:[elimVar]);
        insert res;
      }
      else {
        thisElimCount = [];
        for (Exp subsub <- sub)
          thisElimCount += eliminatedVars[subsub];
        eliminatedVars += (sub:thisElimCount);
        
        res = sub;
        for (elimVar <- toSet(thisElimCount)) {
          elimVarCount = size([e2 | e2 <- thisElimCount, e2 == elimVar]);
          rep = [k | <k,v> <- newvars<0,1>, v == elimVar][0]; 
          targetCount = counts[rep];
          
          if(targetCount == elimVarCount) {
            res = app(lambda(elimVar, sub), rep);
            resElimCount = [v | v <- thisElimCount, v != elimVar];
            eliminatedVars += (res:resElimCount);
            insert res;
          }
        }
        
      }
    }
  };
}

int newvarCount = 0;
str makeNewvar() {
  v = "x<newvarCount>";
  newvarCount += 1;
  return v;
}

bool shouldReplace(plus(e1, e2)) = true;
bool shouldReplace(app(e1, e2)) = true;
default bool shouldReplace(e) = false;

Maybe[tuple[str, map[Exp, str]]] eliminateCommon(Exp sub, map[Exp, str] newvars, map[Exp, int] counts, NameGraph G) {
  if ([rep] := [rep | rep <- counts, equal(rep, sub, G)]) {
    str newvar;
    if (rep in newvars)
      newvar = newvars[rep];
    else {
      newvar = makeNewvar();
      newvars += (rep:newvar);
    }
    return just(<newvar, newvars>);
  }
  return nothing();
}

Exp bindEliminated(Exp e, map[Exp, str] newvars) {
  for (<rep, <var, count>> <- newvars<0,1>) {
    occurrences = [v | /str v <- e, v == var];
    if (size(occurrences) == count)
      e = app(lambda(var, e), rep);
  };
  return e;
}
