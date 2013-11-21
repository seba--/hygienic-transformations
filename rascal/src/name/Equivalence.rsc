module name::Equivalence

import name::Names;
import name::Relation;
import name::Rename;

import util::Maybe;
import List;
import Node;
import IO;
import Set;

//ID ident(ID i) = i;
//
//alias Subst = map[ID,ID];
//
//&V tryGet(&K k, map[&K,&V] m, &V v) = m[k] when k in m;
//default &V tryGet(&K k, map[&K,&V] m, &V v) = v;
//&T tryGet(&T t, map[&T,&T] m) = tryGet(t, m, t);
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



bool labelEquivalent(str x1, str x2) = getID(x1) == getID(x2);
bool labelEquivalent(int x1, int x2) = x1 == x2;
bool labelEquivalent(list[&T] xs1, list[&T] xs2) = ( true | it && labelEquivalent(x1,x2) | <x1,x2> <- zip(xs1,xs2))
  when size(xs1) == size(xs2);
bool labelEquivalent(node x1, node x2) = labelEquivalent(getChildren(x1), getChildren(x2))
  when getName(x1) == getName(x2);
default bool labelEquivalent(x1, x2) {
  println(x1);
  println(x2);
  return false;
}

bool alphaEquivalent(t1, <V1,E1,N1>, t2, <V2,E2,N2>) {
  if (!labelEquivalent(t1, t2))
    return false;
  
  map[ID,str] unique1 = (id:"x_<n>" | <id,n> <- zip(toList(N1<0>), [0..size(N1<0>)]));
  map[ID,str] unique2 = (id:"x_<n>" | <id,n> <- zip(toList(N2<0>), [0..size(N1<0>)]));
  
  t1r = rename(E1, t1, unique1);
  t2r = rename(E2, t2, unique2);
  
  println(t1r);
  println(t2r);
  return t1r == t2r;
}
