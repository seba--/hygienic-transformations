package name

import scala.language.implicitConversions

/**
 * Created by seba on 02/08/14.
 */
object Name {
  private var id = 0
  private def nextID(): Int = { val n = id; id+=1; n }
  class ID(id: Int, var nameO: Name) {
    def this(name0: Name) = this(nextID, name0)
    def name = nameO.name

    override def toString = s"${nameO.name}@$id"
  }

  implicit def apply(name: String) = {
    val id = new ID(null)
    val nameO = new Name(name, id)
    id.nameO = nameO
    nameO
  }
  def apply(name: String, id: ID) = new Name(name, id)
}

class Name(val name: String, val id: Name.ID) {
  override def equals(a: Any) = a.isInstanceOf[Name] && a.asInstanceOf[Name].name == name
  override def hashCode = name.hashCode
  override def toString = name

  def fresh = Name(name)
}
