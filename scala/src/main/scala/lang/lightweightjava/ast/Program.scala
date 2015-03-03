package lang.lightweightjava.ast

import name.namegraph.NameGraphExtended
import name.{Identifier, Name, Renaming}

case class Program(classes: ClassDefinition*) extends AST {
  override def allNames = classes.flatMap(_.allNames).toSet ++ ObjectClass.allNames

  override def rename(renaming: Renaming) = Program(classes.map(_.rename(renaming)): _*)

  def typeCheck = {
    require(classes.map(_.className.name).distinct.size == classes.size, "All class definition names need to be unique")
    classes.map(_.typeCheckForProgram(this))
  }

  def getClassDefinition(name: ClassName) = {
    val matchingClasses = classes.toSet.filter(_.className.name == name.name)
    if (matchingClasses.size <= 1) matchingClasses.headOption
    else throw new IllegalArgumentException("Multiple class definitions for class name '" + name.name + "' found!")
  }

  def getInheritancePath(classDefinition: ClassDefinition, currentPath: Seq[ClassDefinition] = Seq()): Seq[ClassDefinition] =
    classDefinition.superClass match {
      case ObjectClass => classDefinition +: currentPath
      case superClassName:ClassName =>
        if (currentPath.contains(classDefinition)) throw new IllegalArgumentException("Encountered cyclic inheritance for class '" + classDefinition.className.name + "'")
        else getInheritancePath(getClassDefinition(superClassName).getOrElse(
          throw new IllegalArgumentException("Could not find definition for super class '" + superClassName + "' of class '" + classDefinition.className.name + "'")),
          classDefinition +: currentPath)
    }

  def checkSubclass(subClass : ClassDefinition, parentClass : ClassDefinition): Boolean = subClass == parentClass ||
    getInheritancePath(subClass).contains(parentClass)

  def checkSubclass(subClass : ClassRef, parentClass : ClassRef): Boolean = parentClass == ObjectClass || (subClass != ObjectClass &&
    checkSubclass(getClassDefinition(subClass.asInstanceOf[ClassName]).get, getClassDefinition(parentClass.asInstanceOf[ClassName]).get))

  def getClassFields(classDefinition: ClassDefinition) =
    getInheritancePath(classDefinition).flatMap(_.fields).toSet

  def getClassMethods(classDefinition: ClassDefinition) =
    getInheritancePath(classDefinition).reverse.flatMap(_.methods)

  def findMethod(classDefinition: ClassDefinition, methodName: Name) = getClassMethods(classDefinition).find(_.signature.methodName.name == methodName)

  def findField(classDefinition: ClassDefinition, fieldName: Name) = getClassFields(classDefinition).find(_.fieldName.name == fieldName)

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = {
    // Generate the class name environment for the whole program, where each class name is mapped to a set of corresponding classes,
    // each with a map for field names and one for method names
    val programEnvironment: ClassNameEnvironment = nameEnvironment ++ classes.toSet[ClassDefinition].map(
      c => (c.className,
        getClassFields(c).map(_.fieldName).groupBy(_.name),
        getClassMethods(c).toSet[MethodDefinition].map(_.signature.methodName).groupBy(_.name)
        )).groupBy(_._1.name)

    var duplicateReferences: Map[Identifier, Set[Identifier]] = Map()
    for ((name, classes) <- programEnvironment) {
      if (classes.size > 1)
        duplicateReferences ++= classes.map(_._1).map(c => (c, classes.map(_._1.asInstanceOf[Identifier])))

      for (classEnv <- classes) {
        for ((fieldName, fields) <- classEnv._2) {
          if (fields.size > 1)
            duplicateReferences ++= fields.map(f => (f, fields))
        }

        for ((methodName, methods) <- classEnv._3) {
          if (methods.size > 1)
            duplicateReferences ++= methods.map(m => (m, methods))
        }
      }
    }
    classes.foldLeft(NameGraphExtended(Set(), Map()))(_ + _.resolveNames(programEnvironment)) + NameGraphExtended(Set(), duplicateReferences)
  }

  override def toString = classes.foldLeft("")(_ + _.toString + "\n\n")
}
