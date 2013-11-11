module name::Rename

import name::Gensym;
import name::HygienicCorrectness;
import name::Relation;
import name::Names;
import IO;
import Map;
import String;


&T concatRename(NameGraph G, &T t, ID varLoc, str new) {
  return visit (t) {
       
    case str x => x + deleteOrigin(new) 
      // this means the label/origins of the new name will
      // be the same as the label/origins of x
      
      when

        // x is varLoc itself (either decl or use)
        getID(x) == varLoc
    
        // x is a use of decl varLoc
        || varLoc == refOf(getID(x), G)
        
        // x and varLoc are uses of the same decl
        || refOf(varLoc, G) == refOf(getID(x), G)
        
        // x is the declaration of varLoc
        || getID(x) == refOf(varLoc, G)
  };
}

&T concatRename(NameGraph G, &T t, map[ID, str] subst) {
  return visit (t) {
    case str x: {
      str suff;
      if (getID(x) in subst) {
        // x is the thing in subst itself (either decl or use)
        suff = subst[getID(x)];
      }
      else if (refOf(getID(x), G) in subst) {
        // x is a use of some decl in subst varLoc
        suff = subst[refOf(getID(X), G)];
      }
      else if (id <- subst, refOf(id, G) == refOf(getID(x), G)) {
        // there is a use in subst the decl of which is the decl of x
        suff = subst[id];
      }
      else if (id <- subst, getID(x) == refOf(id, G)) {
        // the is a use in subst x is the declaration.
        suff = subst[id];
      }
      else {
        fail;
      }
      insert x + deleteOrigin(suff);
    }
  };
}


&T concatRename(Edges refs, &T t, map[ID,str] subst) {
  return visit (t) {
    case str x => x + deleteOrigin(subst[getID(x)]) 
      when getID(x) in subst
    case str x => x + deleteOrigin(subst[def]) 
      // XXX: fails to call `refOf`
      // when def := refOf(x@location, refs) && def in subst
      when getID(x) in refs, def := refs[getID(x)], def in subst 
  };
}

// The functions below are *wrong*. But updating
// origins needs to respect the invariants of origins.

//&T rename(NameGraph G, &T t, ID varLoc, str new) {
//  return visit (t) {
//    case str x => setID(new, getOneFrom(getID(x)))
//     // TODO: getOneFrom is WRONG!!! 
//      when getID(x) == varLoc
//    case str x => setID(new, getOneFrom(getID(x))) 
//      when varLoc == refOf(getID(x), G)
//  };
//}
//
//&T rename(Edges refs, &T t, map[ID,str] subst) {
//  return visit (t) {
//    case str x => subst[getID(x)] 
//      when getID(x) in subst
//    case str x => subst[def] 
//      // XXX: fails to call `refOf`
//      // when def := refOf(x@location, refs) && def in subst
//      when getID(x) in refs, def := refs[getID(x)], def in subst 
//  };
//}


//&T fixHygiene(NameGraph Gs, NameGraph Gt, &T t, &U(str) name2var) {
//  Edges badRefs = sourceNotPreserved(Gs, Gt) + synthesizedCaptured(Gs, Gt);
//  synth = synthesizedNodes(Gs, Gt);
//  set[loc] renameLocs 
//    = ({} | it + (l1 in synth ? {l1} : {}) + (l2 in synth ? {l2} : {}) 
//          | <l1,l2> <- badRefs<0,1> );
//  
//  usedNames = namesOf(Gt);
//  map[loc, &U] subst = ();
//  for (l <- renameLocs) {
//    str fresh = freshName(usedNames, Gt.N[l]);
//	usedNames += fresh;
//	freshVar = name2var(fresh);
//	subst += (l:freshVar);
//  };
//  
//  renamed = rename(Gt[1] - badRefs, t, subst);
//  return renamed;
//}

@doc {
  Cleaner paper version of fixHygiene that produces exactly the same result.
}
&T fixHygiene(&S s, &T t, NameGraph(&S) resolveS, NameGraph(&T) resolveT) {
  Gs = <Vs,Es,Ns> = resolveS(s);
  Gt = <Vt,Et,Nt> = resolveT(t);
  
  badDefRefs = ( u:d | <u,d> <- Et<0,1>, u in Vs, u != d, u in Es ? Es[u] != d : true);
  badUseRefs = ( u:d | <u,d> <- Et<0,1>, u notin Vs, d in Vs);
  badNodes = badDefRefs<1> + badUseRefs<1>;
  goodDefRefs = ( u:Es[u] | <u,d> <- badDefRefs<0,1>, u in Es );
  
  //iprintln(badDefRefs);
  //iprintln(badUseRefs);
  //iprintln(goodDefRefs);
  
  if (badNodes == {})
    return t;
  
  usedNames = Nt<1>;
  subst = ();
  
  for (l <- badNodes) {
    <fresh, suff> = freshSuffix(usedNames, nameOf(l, Gt));
    usedNames += fresh;
    subst += (l : suff);
  };
  
  Et_new = Et - (badDefRefs + badUseRefs) + goodDefRefs;
  
  t_new = concatRename(Et_new, t, subst);
  return fixHygiene(s, t_new, resolveS, resolveT);
}
