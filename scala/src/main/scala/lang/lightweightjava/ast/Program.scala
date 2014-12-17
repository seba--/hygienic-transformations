package lang.lightweightjava.ast

import name.{Name, NameGraph}

case class Program(classes: ClassDefinition*) extends AST {
  override def allNames = classes.foldLeft(Set[Name.ID]())(_ ++ _.allNames) ++ ObjectClass.allNames

  override def rename(renaming: RenamingFunction) = Program(classes.map(_.rename(renaming)): _*)

  def typeCheck = {
    require(classes.map(_.className.className).distinct.size == classes.size, "All class definition names need to be unique")
    classes.map(_.typeCheckForProgram(this))
  }

  def getClassDefinition(name: ClassName) = classes.find(_.className == name)

  def getInheritancePath(classDefinition: ClassDefinition, currentPath: Seq[ClassDefinition] = Seq()): Seq[ClassDefinition] =
    classDefinition.superClass match {
      case ObjectClass => classDefinition +: currentPath
      case ClassName(superClassName) =>
        if (currentPath.contains(classDefinition)) throw new IllegalArgumentException("Encountered cyclic inheritance for class '" + classDefinition.className.className + "'")
        else getInheritancePath(getClassDefinition(ClassName(superClassName)).getOrElse(
          throw new IllegalArgumentException("Could not find definition for super class '" + superClassName + "' of class '" + classDefinition.className.className + "'")),
          classDefinition +: currentPath)
    }

  def checkSubclass(subClass : ClassDefinition, parentClass : ClassDefinition) : Boolean = subClass == parentClass ||
    getInheritancePath(subClass).contains(parentClass)

  def checkSubclass(subClass : ClassRef, parentClass : ClassRef) : Boolean = parentClass == ObjectClass || (subClass != ObjectClass &&
    checkSubclass(getClassDefinition(subClass.asInstanceOf[ClassName]).get, getClassDefinition(parentClass.asInstanceOf[ClassName]).get))

  def getClassFields(classDefinition: ClassDefinition): Set[FieldDeclaration] =
    getInheritancePath(classDefinition).flatMap(_.elements.collect({ case f: FieldDeclaration => f})).toSet

  def getClassMethods(classDefinition: ClassDefinition): Seq[MethodDefinition] =
    getInheritancePath(classDefinition).reverse.flatMap(_.elements.collect({ case m: MethodDefinition => m}))

  def findMethod(classDefinition: ClassDefinition, methodName: Name) = getClassMethods(classDefinition).find(_.signature.methodName == methodName)

  def findField(classDefinition: ClassDefinition, fieldName: Name) = getClassFields(classDefinition).find(_.fieldName == fieldName)

  override def resolveNames(nameEnvironment: ClassNameEnvironment): NameGraph = {
    // Generate the class name environment for the whole program, where each class name is mapped to a list of field names and one of method names
    val programEnvironment = nameEnvironment ++ classes.map(c =>
      (c.className.className, (c.className.className.id,
        getClassFields(c).map(f => (f.fieldName, f.fieldName.id)).toMap[Name, Name.ID],
        getClassMethods(c).map(m => (m.signature.methodName, m.signature.methodName.id)).toMap[Name, Name.ID])
      )).toMap[Name, (Name.ID, Map[Name, Name.ID], Map[Name, Name.ID])] +
        (ObjectClass.className -> (ObjectClass.className.id, Map[Name, Name.ID](), Map[Name, Name.ID]()))

    // Add references from methods overriding super-class methods to their overriding counterparts
    val methodOverrideReferences = classes.map(c => (c, c.superClass)).collect {
      case (classDef, superClass@ClassName(name)) =>
        classDef.elements.collect({
          case method@MethodDefinition(MethodSignature(_, _, methodName, _*), _) => (method, methodName) }).map(m => (m._2.id,
            findMethod(getClassDefinition(superClass).get, m._2).getOrElse(m._1).signature.methodName.id)).toMap[Name.ID, Name.ID]
    }.foldLeft(Map[Name.ID, Name.ID]())(_ ++ _)
    classes.foldLeft(NameGraph(Set(), Map(), Set()))(_ ++ _.resolveNames(programEnvironment)) ++ NameGraph(Set(), methodOverrideReferences, Set())
  }

  override def toString = classes.foldLeft("")(_ + _.toString + "\n\n")
}
