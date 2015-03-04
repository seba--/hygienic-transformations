package lang.lightweightjava.ast

import name.namegraph.NameGraphExtended
import name.{Name, Renaming}

case class Program(classes: ClassDefinition*) extends AST {
  override def allNames = classes.flatMap(_.allNames).toSet ++ ObjectClass.allNames

  override def rename(renaming: Renaming) = Program(classes.map(_.rename(renaming)): _*)

  def typeCheck = {
    require(classes.map(_.className.name).distinct.size == classes.size, "All class definition names need to be unique")
    classes.foreach(_.typeCheckForProgram(this))
  }

  def findClassDefinition(name: ClassName) = {
    val matchingClasses = classes.toSet.filter(_.className.name == name.name)
    if (matchingClasses.size <= 1) matchingClasses.headOption
    else throw new IllegalArgumentException("Multiple class definitions for class name '" + name.name + "' found!")
  }

  def computeInheritancePath(classDefinition: ClassDefinition, currentPath: Seq[ClassDefinition] = Seq()): Seq[ClassDefinition] =
    classDefinition.superClass match {
      case ObjectClass => classDefinition +: currentPath
      case superClassName:ClassName =>
        if (currentPath.contains(classDefinition)) throw new IllegalArgumentException("Encountered cyclic inheritance for class '" + classDefinition.className.name + "'")
        else if (findClassDefinition(superClassName).isDefined) computeInheritancePath(findClassDefinition(superClassName).get, classDefinition +: currentPath)
        else classDefinition +: currentPath
    }

  def checkSubclass(subClass : ClassDefinition, parentClass : ClassDefinition): Boolean = subClass == parentClass ||
    computeInheritancePath(subClass).contains(parentClass)

  def checkSubclass(subClass : ClassRef, parentClass : ClassRef): Boolean = parentClass == ObjectClass || (subClass != ObjectClass &&
    checkSubclass(findClassDefinition(subClass.asInstanceOf[ClassName]).get, findClassDefinition(parentClass.asInstanceOf[ClassName]).get))

  def findAllFields(classDefinition: ClassDefinition) =
    computeInheritancePath(classDefinition).flatMap(_.fields).toSet

  def findAllMethods(classDefinition: ClassDefinition) =
    computeInheritancePath(classDefinition).reverse.flatMap(_.methods)

  def findMethod(classDefinition: ClassDefinition, methodName: Name) = findAllMethods(classDefinition).find(_.signature.methodName.name == methodName)

  def findField(classDefinition: ClassDefinition, fieldName: Name) = findAllFields(classDefinition).find(_.fieldName.name == fieldName)

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = {
    // Generate the class name environment for the whole program, where each class name is mapped to a set of corresponding classes,
    // each with a map for field names and one for method names
    val programEnvironment: ClassNameEnvironment = nameEnvironment ++ classes.toSet[ClassDefinition].map(
      c => (c.className,
        findAllFields(c).map(_.fieldName).groupBy(_.name),
        findAllMethods(c).toSet[MethodDefinition].map(_.signature.methodName).groupBy(_.name)
        )).groupBy(_._1.name)

    classes.foldLeft(NameGraphExtended(Set(), Map()))(_ + _.resolveNames(programEnvironment)) + NameGraphExtended(Set(), Map())
  }

  override def toString = classes.foldLeft("")(_ + _.toString + "\n\n")
}
