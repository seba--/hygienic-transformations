package name

import name.Identifier._

import scala.language.implicitConversions

object Identifier {
  class ID(/*var nameO: Identifier*/)

  implicit def apply(name: Name): Identifier = {
    new Identifier(name)
  }

  def unapply(ident: Identifier) = Some(ident.name)
}

// Replacement of previously used "Name" class
// The main difference is that two identifiers with the same name are not equal (=> Identifier behaves similar to Name.ID previously)
// Since identifiers are used and stored more often (e. g. as part of an AST) and getting the name of an identifier is a lot more
// common than finding the identifiers for a name, this variant is more intuitive to use in most cases.
class Identifier(val name: Name) {
  protected var _id: Identifier.ID = new ID()
  def id = _id
  protected var oName: Name = name

  def this(name: Name, ident: Identifier) {
    this(name)
    if (ident != null)
      this._id = ident._id
  }
  def this(name: Name, id: ID) {
    this(name)
    this._id = id
  }

  override def toString = name
  override def equals(obj: scala.Any) = obj.isInstanceOf[Identifier] && obj.asInstanceOf[Identifier]._id == _id
  override def hashCode(): Int = _id.hashCode()

  // Gets the original name of the identifier before any renamings were applied
  def originalName = oName

  // Renames the identifier, resulting in an identifier that equals this one but has a different name
  def rename(newName : Name) = {
    val renamed = new Identifier(newName, this)
    renamed.oName = oName
    renamed
  }

  // Gets a new identifier that has the same name as this one but is not equal
  def fresh = Identifier(name)
}