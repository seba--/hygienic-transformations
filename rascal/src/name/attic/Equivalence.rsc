module name::attic::Equivalence

import name::IDs;
import name::NameGraph;
//import name::NameFix;

import util::Maybe;
import List;
import Node;
import IO;
import Set;

//ID ident(ID i) = i;
//
//alias Subst = map[ID,ID];
//
&V tryGet(&K k, map[&K,&V] m, &V v) = m[k] when k in m;
default &V tryGet(&K k, map[&K,&V] m, &V v) = v;
&T tryGet(&T t, map[&T,&T] m) = tryGet(t, m, t);
//
//Subst labelMatchingID(ID x1, ID x2) = ()
//  when x1 == x2;
//Subst labelMatchingID(ID x1, ID x2) = (x1:x2)
//  when x1 != x2;
//
//Maybe[Subst] labelMatching(str x1, str x2, Subst s) =
//  just(labelMatchingID(tryGet(getID(x1), s), tryGet(getID(x2), s)));
//Maybe[Subst] labelMatching(int x1, int x2, Subst s) = just(()) when x1 == x2;
//Maybe[Subst] labelMatching(list[&T] xs1, list[&T] xs2, Subst s) {
//  if (size(xs1) != size(xs2)) {
//    println(xs1);
//    println(xs2);
//    return nothing();
//  }
//  
//  for (<x1,x2> <- zip(xs1,xs2)) {
//    maybeSubst = labelMatching(x1,x2,s);
//    if (maybeSubst == nothing())
//      return nothing();
//    s += maybeSubst.val;
//  };
//  
//  return just(s);
//}
//Maybe[Subst] labelMatching(node t1, node t2, Subst s) {
//  if (getName(t1) != getName(t2)) {
//    println(t1);
//    println(t2);
//    return nothing();
//  }
//  
//  return labelMatching(getChildren(t1), getChildren(t2), s); 
//} 
//default Maybe[Subst] labelMatching(t1,t2,_) {
//  println(t1);
//  println(t2);
//  return nothing();
//}
//
//Maybe[Subst] labelMatching(t1,t2) = labelMathing(t1,t2,());


bool structurallyEquivalent(str x1, str x2) = true;
bool structurallyEquivalent(int x1, int x2) = x1 == x2;
bool structurallyEquivalent(list[&T] xs1, list[&T] xs2) = ( true | it && structurallyEquivalent(x1,x2) | <x1,x2> <- zip(xs1,xs2))
  when size(xs1) == size(xs2);
bool structurallyEquivalent(node x1, node x2) = structurallyEquivalent(getChildren(x1), getChildren(x2))
  when getName(x1) == getName(x2);
default bool structurallyEquivalent(x1, x2) {
  println(x1);
  println(x2);
  return false;
}

bool alphaEquivalent(t1, <V1,E1,N1>, t2, <V2,E2,N2>) {
  if (!structurallyEquivalent(t1, t2))
    return false;
  if (V1 == V2 && E1 == E2)
    return true;
  // TODO if labels differ, unify labels first and check graphs again
  return false;
}

bool subAlphaEquivalent(t1, t2, <V,E,N>) {
  if (!structurallyEquivalent(t1, t2))
    return false;

  V1 = idsOf(t1);
  for (vr <- V1 & E<0>) {
    vd = E[vr];
    if (nameAt(vr, t1) == nameAt(vd, t1) && 
        nameAt(vr, t2) != nameAt(vd, t2) ||
        nameAt(vr, t1) != nameAt(vd, t1) && 
        nameAt(vr, t2) == nameAt(vd, t2)) {
      println("not sub-alpha (src) at <vr>=<vd>: \n<t1>\n<t2>");
      return false;
    }
  }
  
  synNames = {};
  for (v1 <- V1 - V, v2 <- V1 - V)
    if (nameAt(v1, t1) == nameAt(v2, t1) && 
        nameAt(v1, t2) != nameAt(v2, t2) ||
        nameAt(v1, t1) != nameAt(v2, t1) && 
        nameAt(v1, t2) == nameAt(v2, t2)) {
      println("not sub-alpha (syn) at <v1>=<v2>: \n<t1>\n<t2>");
      return false;
    }

  return true;
}

//Maybe[tuple[map[str,int],map[str,int]]] unifyNames(set[tuple[str,str]] ns) = unifyNames(ns, <(),()>, 0);
//
//Maybe[tuple[map[str,int],map[str,int]]] unifyNames(set[tuple[str,str]] ns, tuple[map[str,int],map[str,int]] m, int i) {
//  if (ns == {})
//    return just(m);
//  
//  <<x,y>,rest> = takeOneFrom(ns);
//  
//  <m1,m2> = m;
//  
//  res = ();
//  if (x in m1 && y in m2) {
//    if (m1[x] == m2[y])
//      return unifyNames(rest, m, i);
//    else
//      return nothing();
//  }
//  else if (x notin m1 && y notin m2)
//    return unifyNames(rest, <m1 + (x:i),m2 + (y:i)>, i+1);
//  else
//    return nothing();
//}
