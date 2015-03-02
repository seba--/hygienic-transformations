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
    getInheritancePath(classDefinition).flatMap(_.elements.collect({ case f: FieldDeclaration => f})).toSet

  def getClassMethods(classDefinition: ClassDefinition) =
    getInheritancePath(classDefinition).reverse.flatMap(_.elements.collect({ case m: MethodDefinition => m}))

  def findMethod(classDefinition: ClassDefinition, methodName: Name) = getClassMethods(classDefinition).find(_.signature.methodName.name == methodName)

  def findField(classDefinition: ClassDefinition, fieldName: Name) = getClassFields(classDefinition).find(_.fieldName.name == fieldName)

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = {
    val classesMap = classes.map(_.className.name).map(name => (name, classes.toSet.filter(_.className.name == name))).toMap

    // Generate the class name environment for the whole program, where each class name is mapped to a set of corresponding classes,
    // each with a map for field names and one for method names
    val programEnvironment: ClassNameEnvironment = nameEnvironment ++ classesMap.map(n => (n._1, n._2.map(c => (c.className,
      getClassFields(c).map(_.fieldName.name).map(fn => (fn, getClassFields(c).map(_.fieldName).filter(_.name == fn))).toMap[Name, Set[Identifier]],
      getClassMethods(c).map(_.signature.methodName.name).map(mn => (mn, getClassMethods(c).map(_.signature.methodName).toSet.filter(_.name == mn))).toMap[Name, Set[Identifier]]
      )))).toMap[Name, Set[(ClassName, Map[Name, Set[Identifier]], Map[Name, Set[Identifier]])]]

    var duplicateReferences: Map[Identifier, Set[Identifier]] = Map()
    for ((name, classes) <- programEnvironment) {
      if (classes.size > 1)
        duplicateReferences ++= classes.map(c => (c._1, classes.map(_._1).toSet[Identifier])).toMap

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
