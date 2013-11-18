module name::RenameString

import String;
import List;
import util::Maybe;
import name::Relation;
import name::Names;
import name::Gensym;

@doc{
This applies a (definition-)renaming to a string (represent as an lrel with origins). 
}
lrel[Maybe[loc], str] renameString(Edges refs, lrel[Maybe[loc], str] src, map[ID,str] subst) {
  src = for (<just(loc l), str x> <- src) {
    if ({l} in subst) {
      //println("Renaming <l>");
      append <just(l), subst[{l}]>;
    }
    else if ({l} in refs, def := refs[{l}], def in subst) {
      //println("Renaming definition <def>");
      append <just(l), subst[def]>;
    } 
    else {
      append <just(l), x>;
    }
  }
  return src;
}


lrel[Maybe[loc], str] fixHygieneString(NameGraph Gs, lrel[Maybe[loc], str] t, NameGraph(lrel[Maybe[loc], str]) resolveT) 
  = fixHygiene2(Gs, t, /* renameString, */ resolveT); 


// NB: this is different from  fixHygiene in Rename.rsc...
lrel[Maybe[loc], str] fixHygiene2(<Vs,Es,Ns>, lrel[Maybe[loc], str] t, NameGraph(lrel[Maybe[loc], str]) resolveT) {
  Gt = <Vt,Et,Nt> = resolveT(t);
  
  //println("Source edges:");
  //iprintln(Es);
  //println("Target edges:");
  //iprintln(Et);
  //println("Synthesized nodes");
  //iprintln(Vt - Vs);
  
  
  notPreserveSourceBinding =    (u:Et[u] | u <- Vs & Vt, u in Es, u in Et && Es[u] != Et[u]);
  //notPreserveDefinitionScope =  (u:Et[u] | d <- Vs & Vt, u <- Et, Et[u] == d, u in Es ? Es[u] != d : true);
  notSafeDefinitionReferences = (u:Et[u] | u <- Vs & Vt, u notin Es, u in Et, Et[u] != u);
  
  //println("not preserve source binding:");
  //iprintln(notPreserveSourceBinding);
  //println("not preserve definition scope:");
  //iprintln(notPreserveDefinitionScope);
  //println("not safe definition references:");
  //iprintln(notSafeDefinitionReferences);

  allBadRefs = notPreserveSourceBinding /*+ notPreserveDefinitionScope*/ + notSafeDefinitionReferences;
  badDefinitionNodes = allBadRefs<1>;
  
  goodDefRefs = ( u:Es[u] | u <- notPreserveSourceBinding<0>, u in Es);
  // goodUseRefs required?
  //goodUseRefs = ( u:d | d <- notPreserveDefinitionScope<1>, u <- Es, Es[u] == d);
  
  //println("All bad refs:");
  //iprintln(allBadRefs);
  
  if (badDefinitionNodes == {})
    return t;

  //println("Bad definition nodes:");
  //iprintln(badDefinitionNodes);

  
  usedNames = Nt<1>;
  subst = ();
  
  for (l <- badDefinitionNodes) {
    fresh = freshName(usedNames, nameOf(l, Gt));
    usedNames += fresh;
    subst += (l : fresh);
  };
  
  Et_new = Et - allBadRefs + goodDefRefs;// + goodUseRefs;
  
  //println("New reference graph:");
  //iprintln(Et_new);
  
  t_new = renameString(Et_new, t, subst);
  
  return fixHygiene2(<Vs,Es,Ns>, t_new, resolveT);
}


@doc{
This function maps (target language) name-IDs to (source or meta program) origins
through the reconstructed result origins.
}
NameGraph insertSourceNames(NameGraph targetGraph, lrel[loc, loc, str] reconOrgs, loc orgLoc) {
  return visit (targetGraph) {
    case loc l => sourceLoc
      when <l, loc sourceLoc, _> <- reconOrgs//, sourceLoc.path == orgLoc.path
  }
}


@doc{
An lrel coming from origins()) maps (source or meta program) origins to 
output string fragments. This functions reconstructs the source locations
of each chunk according to occurence in the output. 
}
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
