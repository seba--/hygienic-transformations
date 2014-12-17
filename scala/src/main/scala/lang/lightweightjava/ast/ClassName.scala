package lang.lightweightjava.ast

import name.{Name, NameGraph}

abstract class ClassRef extends AST {
  override def rename(renaming: RenamingFunction): ClassRef

  def className: Name

  override def resolveNames(nameEnvironment: ClassNameEnvironment): NameGraph = {
    // If the class name is pointing to itself (because it is declared here), add only the node but no edges
    if (!nameEnvironment.contains(className) || nameEnvironment(className)._1 == className.id)
      NameGraph(Set(className.id), Map(), Set())
    // If the class name is pointing to another class name, add it and the edge to the name graph
    else
      NameGraph(Set(className.id), Map(className.id -> nameEnvironment(className)._1), Set())
  }

  override def toString: String = className.toString
}

object ObjectClass extends ClassRef {
  private val objectName = Name("Object")

  override def className = objectName

  override def allNames = Set(className.id)

  override def rename(renaming: RenamingFunction) = if (renaming(className).name == "Object") this else ClassName(renaming(className))
}

case class ClassName(className: Name) extends ClassRef {
  require(AST.isLegalName(className), "Class name '" + className + "' is no legal Java class name")
  require(className.name != "Object", "Can't redefine 'Object' class!")

  override def allNames = Set(className.id)

  override def rename(renaming: RenamingFunction) = ClassName(renaming(className))
}