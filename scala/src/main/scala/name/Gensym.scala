package name

/**
 * Created by seba on 01/08/14.
 */
object Gensym {
  def gensym(base: String, used: Set[String]): String = gensym(base, used, 0)

  def gensym(base: String, used: Set[String], n: Int): String = {
    val name = base + "_" + n
    if (!used.contains(name))
      name
    else
      gensym(base, used, n+1)
  }

}
