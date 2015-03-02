package lang.lightweightjava.ast

import name.namegraph.NameGraph
import name.{Identifier, Name, Renaming}

trait ClassRef extends Identifier with AST {
  override def allNames = Set(this)

  override def rename(renaming: Renaming): ClassRef = renameClass(renaming(this).name)

  protected def renameClass(newName : Name) = {
    if (newName == "Object") ObjectClass
    else {
      val renamed = ClassName(newName)
      renamed.id = this.id
      renamed
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment): NameGraph = {
    // If the class name is pointing to itself (because it is declared here), add only the node but no edges
    if (!nameEnvironment.contains(name) || nameEnvironment(name)._1 == this)
      NameGraph(Set(this), Map())
    // If the class name is pointing to another class name, add it and the edge to the name graph
    else
      NameGraph(Set(this), Map(this -> nameEnvironment(name)._1))
  }

  override def toString: String = name
}

object ObjectClass extends Identifier("Object") with ClassRef {
  override def fresh = throw new IllegalArgumentException("Can't create fresh instance of Object class!")
}

case class ClassName(override val name: Name) extends Identifier(name) with ClassRef {
  require(AST.isLegalName(name), "Class name '" + name + "' is no legal Java class name")
  require(name != "Object", "Can't redefine 'Object' class!")

  override def fresh = ClassName(name)
}