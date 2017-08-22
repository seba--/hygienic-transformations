package name

/**
 * Created by seba on 01/08/14.
 */
object GensymPure {
  def gensym(base: Name, used: Set[Name]): Name = gensym(base, used, 0)

  def gensym(base: Name, used: Set[Name], n: Int): Name = {
    val name = base + "_" + n
    if (!used.contains(name))
      name
    else
      gensym(base, used, n+1)
  }
}

class Gensym() {
  var used = Map[Name, Int]()
  def fresh(base: Name): Name = used.get(base) match {
    case None =>
      used += base -> 0
      s"${base}_0"
    case Some(count) =>
      used += base -> (count+1)
      s"${base}_${count+1}"
  }
}