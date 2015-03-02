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
  protected var id: Identifier.ID = new ID(this)

  override def toString = name

  def rename(newName : Name) = {
    val renamed = new Identifier(newName)
    renamed.id = this.id
    renamed
  }

  def fresh = Identifier(name)
}