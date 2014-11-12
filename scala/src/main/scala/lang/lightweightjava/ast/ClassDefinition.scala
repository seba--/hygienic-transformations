package lang.lightweightjava.ast

import name.{Name, NameGraph}

case class ClassDefinition(className: ClassName, superClass: ClassRef, elements: ClassElement*) extends AST {
  require(AST.isLegalName(className.className), "Class name '" + className.className + "' is no legal Java class name")

  override def allNames = elements.foldLeft(Set[Name.ID]())(_ ++ _.allNames) ++ superClass.allNames ++ className.allNames

  override def rename(renaming: Renaming) =
    ClassDefinition(className.rename(renaming), superClass.rename(renaming), elements.map(_.rename(renaming)): _*)

  private def typeCheckInternal(program : Program) = {
    val classFields = program.getClassFields(this)
    val classMethods = program.getClassMethods(this)
    require(className != superClass, "Class '" + className.className + "' can't be it's own super-class")
    superClass match {
      case className@ClassName(_) =>
        val superClassDefinition = program.getClassDefinition(className).get
        require(elements.collect({ case FieldDeclaration(_, _, name) => name }).toSet.intersect(program.getClassFields(superClassDefinition).map(_.fieldName)).size == 0,
          "Class '" + className + "' overshadows fields of it's super-classes")
        require(classMethods.forall(method => program.findMethod(superClassDefinition, method.signature.methodName) match {
          case Some(superClassMethod) => method.signature.accessModifier == superClassMethod.signature.accessModifier &&
            method.signature.returnType == superClassMethod.signature.returnType &&
            method.signature.parameters.map(_.variableType) == superClassMethod.signature.parameters.map(_.variableType)
          case None => true
        }), "Class '" + className + "' overwrites a super-class method with a different access modifier, return type or different parameter types")
      case _ => ;
    }
    require(elements.count(_.isInstanceOf[FieldDeclaration]) == elements.collect({ case FieldDeclaration(_, _, name) => name }).size,
      "Field names of class '" + className.className + "' are not distinct")
    require(elements.count(_.isInstanceOf[MethodDefinition]) == elements.collect({ case MethodDefinition(MethodSignature(_, _, name, _*), _) => name }).size,
      "Method names of class '" + className.className + "' are not distinct")
    require(classFields.map(_.fieldType).forall {
      case className@ClassName(_) => program.getClassDefinition(className).isDefined
      case _ => true
    }, "Could not find definition for some field types of class '" + className.className + "'")
  }

  def typeCheckForProgram(program : Program) = {
    typeCheckInternal(program)
    program.getClassMethods(this).map(_.typeCheckForClassDefinition(program, this))
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment): NameGraph = {
    val classNameGraph = className.resolveNames(nameEnvironment, isExported = true) + superClass.resolveNames(nameEnvironment, isExported = true)

    // Collect all field/method names
    val fieldNames = elements.toSeq.collect({ case FieldDeclaration(_, _, name) => name })
    val methodNames = elements.toSeq.collect({ case MethodDefinition(MethodSignature(_, _, name, _*), _) => name })

    // Group equal field names and then filter so that only duplicate fields remain
    val doubleFieldNames = fieldNames.foldLeft(Set[Set[Name.ID]]())((oldSet, field) => oldSet.find(_.exists(field.name == _.name)) match {
      case Some(set) => oldSet - set + (set + field.id)
      case None => oldSet + Set(field.id)
    }).filter(_.size > 1)

    // Group equal method names and then filter so that only duplicate method remain
    val doubleMethodNames = methodNames.foldLeft(Set[Set[Name.ID]]())((oldSet, method) => oldSet.find(_.exists(method.name == _.name)) match {
      case Some(set) => oldSet - set + (set + method.id)
      case None => oldSet + Set(method.id)
    }).filter(_.size > 1)

    // Create the error list for the name graph based on the duplicate lists
    val duplicateErrors = (doubleFieldNames ++ doubleMethodNames).toSet[Set[Name.ID]]

    classNameGraph + elements.foldLeft(NameGraph(Set(), Map(), duplicateErrors))(_ + _.resolveNames(nameEnvironment, this))
  }

  override def toString: String = "class " + className.toString +
    (if (superClass != ObjectClass) " extends " + superClass.toString else "") + " {\n\t" + elements.mkString("\n\t") + "}"
}
