package name

import name.Identifier._

import scala.language.implicitConversions

object Identifier {
  class ID(var nameO: Identifier) {
    def name = nameO.name
  }

  implicit def apply(name: Name): Identifier = {
    new Identifier(name)
  }
}

class Identifier(val name: Name) {
  override def equals(obj: scala.Any) = obj.isInstanceOf[Identifier] && obj.asInstanceOf[Identifier].id == id
  override def hashCode(): Int = id.hashCode()

  protected var id: Identifier.ID = new ID(this)
  protected var oName: Name = name

  def originalName = oName
  override def toString = name

  def rename(newName : Name) = {
    val renamed = new Identifier(newName)
    renamed.id = id
    renamed.oName = oName
    renamed
  }

  def fresh = Identifier(name)
}