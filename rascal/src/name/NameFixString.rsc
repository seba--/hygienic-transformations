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

//&T rename(&T t, map[ID,str] subst) {
//  return visit (t) {
//    case str x => setID(subst[getID(x)], getID(x)) 
//      when getID(x) in subst
//  };
//}


/*
This applies a (definition-)renaming to a string (represent as an lrel with origins). 
*/
lrel[Maybe[loc], str] renameString(lrel[Maybe[loc], str] src, map[ID,str] subst) {
  src = for (<just(loc l), str x> <- src) {
    if ([l] in subst) {
      //println("Renaming <l>");
      append <just(l), subst[[l]]>;
    }
    else {
      append <just(l), x>;
    }
  }
  return src;
}


str nameAtString(ID n, lrel[Maybe[loc], str] t) {
  //println("Search for name: <n>");
  for (<just(loc l1), str x> <- t) {
    //println("Loc l1 = <l1>");
    if ([l1] == n) {
      //println("Found it: <x>");
      return x;
    }
  } // |project://Rascal-Hygiene/formats/minbad.derric|(256,1,<21,2>,<21,3>)
  // Non-existing name.... (TODO?)
  //println("No name");
  return "";
}


lrel[Maybe[loc], str] nameFixString(NameGraph Gs, lrel[Maybe[loc], str] t, NameGraph(lrel[Maybe[loc], str]) resolveT, loc outFile) 
  = nameFix(Gs, t, resolveT, renameString, str(ID n, lrel[Maybe[loc], str] t) {
     return nameAtString(n, t); //reconstruct(t, outFile));
     // |project://generated-missgrant/src/MissGrant.java|  
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
