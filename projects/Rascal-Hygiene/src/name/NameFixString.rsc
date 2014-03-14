module name::NameFixString

import String;
import List;
import Set;
import util::Maybe;
import name::NameGraph;
import name::IDs;
import name::Gensym;
import name::HygienicCorrectness;
import name::NameFix;
import IO;

/*
This applies a (definition-)renaming to a string (represent as an lrel with origins). 
*/
lrel[Maybe[loc], str] renameString(lrel[Maybe[loc], str] src, map[ID,str] subst) {
  src = for (<just(loc l), str x> <- src) {
    if ([l] in subst) {
      append <just(l), subst[[l]]>;
    }
    else {
      append <just(l), x>;
    }
  }
  return src;
}


str nameAtString(ID n, lrel[Maybe[loc], str] t) {
  for (<just(loc l1), str x> <- t) {
    if ([l1] == n) {
      return x;
    }
  } 
  // Non-existing name...
  return "";
}


lrel[Maybe[loc], str] nameFixString(NameGraph Gs, lrel[Maybe[loc], str] t, NameGraph(lrel[Maybe[loc], str]) resolveT, loc outFile) 
  = nameFix(Gs, t, resolveT, renameString, str(ID n, lrel[Maybe[loc], str] t) {
     return nameAtString(n, t); 
  });

/*
This function maps (target language) name-IDs to (source or meta program) origins
through the reconstructed result origins.
*/
NameGraph insertSourceNames(NameGraph targetGraph, lrel[loc, loc, str] reconOrgs, loc orgLoc) {
  return visit (targetGraph) {
    case loc l => sourceLoc
      when <l, loc sourceLoc, _> <- reconOrgs//, sourceLoc.path == orgLoc.path
  }
}


/*
An lrel coming from origins()) maps (source or meta program) origins to 
output string fragments. This functions reconstructs the source locations
of each chunk according to occurence in the output. 
*/
lrel[loc, loc, str] reconstruct(lrel[Maybe[loc], str] orgs, loc src) {
  cur = |<src.scheme>://<src.authority><src.path>|(0, 0, <1, 0>, <1,0>);
 
  result = for (<org, str sub> <- orgs) {
    cur.length = size(sub);
    nls = size(findAll(sub, "\n"));
    cur.end.line += nls;
    if (nls != 0) {
      // reset
      cur.end.column = size(sub) - findLast(sub, "\n") - 1;
    }
    else {
      cur.end.column += size(sub);
    }
    if (just(loc l) := org) {
      append <cur, l, sub>;
    }
    else {
      throw "No origin: \'<sub>\'";
    }
    cur.offset += size(sub);
    cur.begin.column = cur.end.column;
    cur.begin.line = cur.end.line;
  }
  
  return result;
}
