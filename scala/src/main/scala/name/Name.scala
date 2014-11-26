package name

import scala.language.implicitConversions

/**
 * Created by seba on 02/08/14.
 */
object Name {
  class ID(var nameO: Name) {
    def name = nameO.name
    override def toString = name
  }

  implicit def apply(name: String): Name = {
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
