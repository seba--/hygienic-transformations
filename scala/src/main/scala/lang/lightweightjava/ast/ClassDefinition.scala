package lang.lightweightjava.ast

import lang.lightweightjava.ClassInterface
import name.namegraph.{NameGraphExtended, NameGraphModular}
import name._

case class ClassDefinition(className: ClassName, superClass: ClassRef, elements: ClassElement*) extends AST with NominalModular[ClassInterface] {
  require(AST.isLegalName(className.name), "Class name '" + className.name + "' is no legal Java class name")

  override def allNames = elements.flatMap(_.allNames).toSet ++ superClass.allNames ++ className.allNames

  val fields = elements.collect({ case f: FieldDeclaration => f }).toSet
  val methods = elements.collect({ case m: MethodDefinition => m }).toSet

  private val exportedFields = fields.filter(_.accessModifier == AccessModifier.PUBLIC).map(_.fieldName)

  private val exportedMethods = methods.filter(_.signature.accessModifier == AccessModifier.PUBLIC).map(_.signature.methodName)

  override val moduleID: Identifier = className

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
        require(fields.map(_.fieldName.name).intersect(program.getClassFields(superClassDefinition).map(_.fieldName.name)).size == 0,
          "Class '" + className + "' overshadows fields of it's super-classes")
        require(classMethods.forall(method => program.findMethod(superClassDefinition, method.signature.methodName.name) match {
          case Some(superClassMethod) => method.signature.accessModifier == superClassMethod.signature.accessModifier &&
            method.signature.returnType.name == superClassMethod.signature.returnType.name &&
            method.signature.parameters.map(_.variableType.name) == superClassMethod.signature.parameters.map(_.variableType.name)
          case None => true
        }), "Class '" + className + "' overwrites a super-class method with a different access modifier, return type or different parameter types")
      case _ => ;
    }
    require(fields.size == fields.map(_.fieldName.name).size,
      "Field names of class '" + className.name + "' are not distinct")
    require(methods.size == methods.map(_.signature.methodName.name).size,
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

  override def resolveNamesModular(metaDependencies: Set[ClassInterface]): (NameGraphModular, ClassInterface) = {
    val classInterface = new ClassInterface(className, exportedFields, exportedMethods)
    var environment: ClassNameEnvironment = Map()
    val ownFieldsMap = fields.map(_.fieldName.name).map(n => (n, fields.map(_.fieldName).filter(_.name == n))).toMap
    val ownMethodsMap = methods.map(_.signature.methodName.name).map(n => (n, methods.map(_.signature.methodName).filter(_.name == n))).toMap
    environment += (className.name -> Set((className, ownFieldsMap, ownMethodsMap)))

    for (dependency <- metaDependencies) {
      if (environment.contains(dependency.moduleID.name))
        throw new IllegalArgumentException("Multiple instances of class '" + dependency.moduleID.name + "' found!")
      else {
        val fieldsMap = dependency.exportedFields.map(_.name).map(n => (n, dependency.exportedFields.filter(_.name == n))).toMap
        val methodsMap = dependency.exportedMethods.map(_.name).map(n => (n, dependency.exportedMethods.filter(_.name == n))).toMap
        environment += (dependency.moduleID.name -> Set((dependency.moduleID, fieldsMap, methodsMap)))
      }
    }

    val nameGraph = resolveNames(environment)
    var intEdges: Map[Identifier, Set[Identifier]] = Map()
    var outEdges: Map[Identifier, Set[Identifier]] = Map()

    for ((v, ds) <- nameGraph.E) {
      intEdges += (v -> ds.filter(d => nameGraph.V.contains(d)))
      outEdges += (v -> ds.filter(d => !nameGraph.V.contains(d)))
    }

    val modularNameGraph = NameGraphModular(nameGraph.V, intEdges.filter(_._2.size > 0), outEdges.filter(_._2.size > 0))

    (modularNameGraph, classInterface)
  }

  override def resolveNamesVirtual(metaDependencies: Set[ClassInterface], renaming: Renaming): NameGraphModular = {
    val dependenciesRenamed = metaDependencies.map(i =>
      new ClassInterface(i.moduleID, i.exportedFields.map(f => renaming(f)), i.exportedMethods.map(f => renaming(f))))
    resolveNamesModular(dependenciesRenamed)._1
  }

  override def toString = "class " + className.toString +
    (if (superClass != ObjectClass) " extends " + superClass.toString else "") + " {\n\t" + elements.mkString("\n\t") + "}"
}
