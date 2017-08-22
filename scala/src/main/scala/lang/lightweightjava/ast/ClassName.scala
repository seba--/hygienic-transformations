package lang.lightweightjava.ast

import name.namegraph.NameGraphExtended
import name.{Identifier, Name, Renaming}

import scala.language.implicitConversions

trait ClassRef extends Identifier with AST {
  override def allNames = Set(this.name)

  override def rename(renaming: Renaming): ClassRef = renameClass(renaming(this).name)

  protected def renameClass(newName : Name) = {
    if (newName == "Object") ObjectClass
    else {
      val renamed = ClassName(newName)
      renamed._id = this._id
      renamed.oName = this.oName
      renamed
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = {
    // If the class name is pointing to itself (because it is declared here), add only the node but no edges
    if (!nameEnvironment.contains(name))
      NameGraphExtended(Set(this), Map())
    // If the class name is pointing to another class name, add it and the edge to the name graph
    else
      NameGraphExtended(Set(this), Map(this -> nameEnvironment(name).map(_._1)))
  }
}

object ObjectClass extends Identifier("Object") with ClassRef {
  override def fresh = throw new IllegalArgumentException("Can't create fresh instance of Object class!")
}

object ClassName {
  implicit def apply(name: Name): ClassName = {
    new ClassName(name)
  }
}

class ClassName(override val name: Name) extends Identifier(name) with ClassRef {
  require(AST.isLegalName(name), "Class name '" + name + "' is no legal Java class name")
  require(name != "Object", "Can't redefine 'Object' class!")

  override def fresh = ClassName(name)
}