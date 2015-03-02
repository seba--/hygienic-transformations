package lang.lightweightjava.ast

import name.namegraph.NameGraph
import name.{Renaming, Identifier, Name}

case class Program(classes: ClassDefinition*) extends AST {
  override def allNames = classes.foldLeft(Set[Identifier]())(_ ++ _.allNames) ++ ObjectClass.allNames

  override def rename(renaming: Renaming) = Program(classes.map(_.rename(renaming)): _*)

  def typeCheck = {
    require(classes.map(_.className.name).distinct.size == classes.size, "All class definition names need to be unique")
    classes.map(_.typeCheckForProgram(this))
  }

  def getClassDefinition(name: ClassName) = classes.find(_.className.name == name.name)

  def getInheritancePath(classDefinition: ClassDefinition, currentPath: Seq[ClassDefinition] = Seq()): Seq[ClassDefinition] =
    classDefinition.superClass match {
      case ObjectClass => classDefinition +: currentPath
      case ClassName(superClassName) =>
        if (currentPath.contains(classDefinition)) throw new IllegalArgumentException("Encountered cyclic inheritance for class '" + classDefinition.className.name + "'")
        else getInheritancePath(getClassDefinition(ClassName(superClassName)).getOrElse(
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
    // Generate the class name environment for the whole program, where each class name is mapped to a list of field names and one of method names
    val programEnvironment = nameEnvironment ++ classes.map(c =>
      (c.className.name, (c.className,
        getClassFields(c).map(f => (f.fieldName.name, f.fieldName)).toMap[Name, Identifier],
        getClassMethods(c).map(m => (m.signature.methodName.name, m.signature.methodName)).toMap[Name, Identifier])
      )).toMap[Name, (Identifier, Map[Name, Identifier], Map[Name, Identifier])] +
        (ObjectClass.name -> (ObjectClass, Map[Name, Identifier](), Map[Name, Identifier]()))

    // Add references from methods overriding super-class methods to their overriding counterparts
    val methodOverrideReferences = classes.map(c => (c, c.superClass)).collect {
      case (classDef, superClass@ClassName(name)) =>
        classDef.elements.collect({
          case method@MethodDefinition(MethodSignature(_, _, methodName, _*), _) => (method, methodName) }).map(m => (m._2,
            findMethod(getClassDefinition(superClass).get, m._2.name).getOrElse(m._1).signature.methodName)).toMap[Identifier, Identifier]
    }.foldLeft(Map[Identifier, Identifier]())(_ ++ _)
    classes.foldLeft(NameGraph(Set(), Map()))(_ + _.resolveNames(programEnvironment)) + NameGraph(Set(), methodOverrideReferences)
  }

  override def toString = classes.foldLeft("")(_ + _.toString + "\n\n")
}
