package lang.lightweightjava.ast

import lang.lightweightjava.ClassInterface
import name.namegraph.{NameGraphExtended, NameGraphModular}
import name._

case class ClassDefinition(className: ClassName, superClass: ClassRef, elements: ClassElement*) extends AST with NominalModular[ClassInterface] {
  require(AST.isLegalName(className.name), "Class name '" + className.name + "' is no legal Java class name")

  override def allNames = elements.flatMap(_.allNames).toSet ++ superClass.allNames ++ className.allNames

  private def exportedFields = elements.collect {
    case FieldDeclaration(AccessModifier.PUBLIC, _, fieldName) => fieldName
  }.toSet

  private def exportedMethods = elements.collect {
    case MethodDefinition(MethodSignature(AccessModifier.PUBLIC, _, methodName, _*), _) => methodName
  }.toSet

  override def moduleID: Identifier = className

  override def resolveNamesModular(metaDependencies: Set[ClassInterface]): (NameGraphModular, ClassInterface) = {
    val classInterface = new ClassInterface(className, exportedFields, exportedMethods)
    (???, classInterface)
  }

  override def resolveNamesVirtual(metaDependencies: Set[ClassInterface], renaming: Renaming): NameGraphModular = {
    val dependenciesRenamed = metaDependencies.map(i => new ClassInterface(i.moduleID, i.exportedFields.map(f => renaming(f)), i.exportedMethods.map(f => renaming(f))))
    resolveNamesModular(dependenciesRenamed)._1
  }

  override def dependencies: Set[Name] = allNames.collect {
    case className:ClassName => className.name
  }

  override def rename(renaming: Renaming) =
    ClassDefinition(className.rename(renaming).asInstanceOf[ClassName], superClass.rename(renaming), elements.map(_.rename(renaming)): _*)

  private def typeCheckInternal(program : Program) = {
    val classFields = program.getClassFields(this)
    val classMethods = program.getClassMethods(this)
    require(className != superClass, "Class '" + className.name + "' can't be it's own super-class")
    superClass match {
      case className:ClassName =>
        val superClassDefinition = program.getClassDefinition(className).get
        require(elements.collect({ case FieldDeclaration(_, _, name) => name.name }).toSet.intersect(program.getClassFields(superClassDefinition).map(_.fieldName.name)).size == 0,
          "Class '" + className + "' overshadows fields of it's super-classes")
        require(classMethods.forall(method => program.findMethod(superClassDefinition, method.signature.methodName.name) match {
          case Some(superClassMethod) => method.signature.accessModifier == superClassMethod.signature.accessModifier &&
            method.signature.returnType.name == superClassMethod.signature.returnType.name &&
            method.signature.parameters.map(_.variableType.name) == superClassMethod.signature.parameters.map(_.variableType.name)
          case None => true
        }), "Class '" + className + "' overwrites a super-class method with a different access modifier, return type or different parameter types")
      case _ => ;
    }
    require(elements.count(_.isInstanceOf[FieldDeclaration]) == elements.collect({ case FieldDeclaration(_, _, name) => name }).size,
      "Field names of class '" + className.name + "' are not distinct")
    require(elements.count(_.isInstanceOf[MethodDefinition]) == elements.collect({ case MethodDefinition(MethodSignature(_, _, name, _*), _) => name }).size,
      "Method names of class '" + className.name + "' are not distinct")
    require(classFields.map(_.fieldType).forall {
      case className:ClassName => program.getClassDefinition(className).isDefined
      case ObjectClass => true
    }, "Could not find definition for some field types of class '" + className.name + "'")
  }

  def typeCheckForProgram(program : Program) = {
    typeCheckInternal(program)
    program.getClassMethods(this).map(_.typeCheckForClassDefinition(program, this))
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = {
    className.resolveNames(nameEnvironment) + superClass.resolveNames(nameEnvironment) + elements.foldLeft(NameGraphExtended(Set(), Map()))(_ + _.resolveNames(nameEnvironment, this))
  }

  override def toString = "class " + className.toString +
    (if (superClass != ObjectClass) " extends " + superClass.toString else "") + " {\n\t" + elements.mkString("\n\t") + "}"
}
