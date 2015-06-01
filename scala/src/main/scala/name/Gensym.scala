package name

/**
 * Created by seba on 01/08/14.
 */
object Gensym {
  def gensym(base: Name, used: Set[Name]): Name = gensym(base, used, 0)

  def gensym(base: Name, used: Set[Name], n: Int): Name = {
    val name = base + "_" + n
    if (!used.contains(name))
      name
    else
      gensym(base, used, n+1)
  }

}