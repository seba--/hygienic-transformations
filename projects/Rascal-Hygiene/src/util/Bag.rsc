module util::Bag

alias Bag[&T] = map[&T,int];

Bag[&T] makeBag(list[&T] ls) {
  res = ();
  for (t <- ls) res = bagAdd(res, t);
  return res;
}

Bag[&T] bagAdd(Bag[&T] b, &T t) = bagAdd(b, t, 1);

Bag[&T] bagAdd(Bag[&T] b, &T t, int count) {
  if (t in b)
    return b + (t:b[t]+count);
  return b + (t:count);
}

Bag[&T] bagAddAll(Bag[&T] b1, Bag[&T] b2) {
  res = b1;
  for (<b,bcount> <- b2<0,1>) res = bagAdd(res, b, bcount);
  return res;
}
