package lang.lightweightjava.ast

import lang.lightweightjava.ClassInterface
import name.namegraph.{NameGraphExtended, NameGraphModular}
import name._

case class ClassDefinition(className: ClassName, superClass: ClassRef, elements: ClassElement*) extends NominalModular[ClassInterface] {

  private var dependencies = Set[ClassInterface]()
  override def link(dependencies: Set[ClassInterface]) = {
    this.dependencies = dependencies
    val newInterface = ClassInterface(className, exportedFields, exportedMethods)
    if (_interface != null)
      newInterface.original = _interface
    _interface = newInterface
    _resolved = null
    this
  }

  require(AST.isLegalName(className.name), "Class name '" + className.name + "' is no legal Java class name")

  override def allNames = elements.flatMap(_.allNames).toSet ++ superClass.allNames ++ className.allNames

  val fields = elements.collect({ case f: FieldDeclaration => f }).toSet
  val methods = elements.collect({ case m: MethodDefinition => m }).toSet

  private def exportedFields = {
    val ownFields = fields.filter(_.accessModifier == AccessModifier.PUBLIC).map(_.fieldName)
    val superFields = dependencies.find(_.className.name == superClass.name) match {
      case Some(i) => i.exportedFields
      case None => Set()
    }
    superFields ++ ownFields
  }
  private def exportedMethods = {
    val ownMethods = methods.filter(_.signature.accessModifier == AccessModifier.PUBLIC).map(_.signature.methodName)
    val superMethods = dependencies.find(_.className.name == superClass.name) match {
      case Some(i) => i.exportedMethods
      case None => Set()
    }
    superMethods.filter(m => !ownMethods.exists(_.name == m.name)) ++ ownMethods
  }

  val moduleID: Identifier = className


  protected var _interface: ClassInterface = null
  def interface = {
    if (_interface == null)
      _interface = ClassInterface(className, exportedFields, exportedMethods)

    _interface
  }

  def rename(renaming: Renaming) = {
    val cl = ClassDefinition(className.rename(renaming).asInstanceOf[ClassName], superClass.rename(renaming), elements.map(_.rename(renaming)): _*)
    cl.link(dependencies)
    cl.interface.original = interface.original
    cl
  }

  def typeCheckForProgram(program : Program) = {
    val classFields = program.findAllFields(this)
    val classMethods = program.findAllMethods(this)

    require(className != superClass, "Class '" + className.name + "' can't be it's own super-class")

    superClass match {
      case superClassName:ClassName =>
        val superClassDefinition = program.findClassDefinition(superClassName)

        require(superClassDefinition.isDefined, "Super-class '" + superClassName.name + "' of class '" + className.name + "' can't be resolved")

        require(fields.map(_.fieldName.name).intersect(program.findAllFields(superClassDefinition.get).map(_.fieldName.name)).size == 0,
          "Class '" + className + "' overshadows fields of it's super-classes")

        require(classMethods.forall(method => program.findMethod(superClassDefinition.get, method.signature.methodName.name) match {
          case Some(superClassMethod) =>
            method.signature.accessModifier == superClassMethod.signature.accessModifier &&
            method.signature.returnType.name == superClassMethod.signature.returnType.name &&
            method.signature.parameters.map(_.variableType.name) == superClassMethod.signature.parameters.map(_.variableType.name)

          case None => true

        }), "Class '" + className + "' overwrites a super-class method with a different access modifier, return type or different parameter types")

      case _ => ; // Skip if super-class is Object class
    }
    require(fields.size == fields.map(_.fieldName.name).size, "Field names of class '" + className.name + "' are not distinct")

    require(methods.size == methods.map(_.signature.methodName.name).size, "Method names of class '" + className.name + "' are not distinct")

    require(classFields.map(_.fieldType).forall {
      case className:ClassName =>
        program.findClassDefinition(className).isDefined

      case ObjectClass => true

    }, "Could not find definition for some field types of class '" + className.name + "'")

    program.findAllMethods(this).foreach(_.typeCheckForClassDefinition(program, this))
  }

  def resolveNames(nameEnvironment: ClassNameEnvironment) = {
    var conflictReferences: Map[Identifier, Set[Identifier]] = Map()

    // All classes in the environment that share the same name as this class
    val classes = nameEnvironment(className.name)

    // Add references for conflicting classes
    if (classes.size > 1)
      conflictReferences += (className -> (classes.map(_._1.asInstanceOf[Identifier]) - className))

    // Find this class by comparing IDs instead of names
    val ownClass = classes.find(_._1 == className).get

    for (classEnv <- classes) {
      // Add references for conflicting fields
      for ((fieldName, fields) <- classEnv._2) {
         if (ownClass._2.contains(fieldName) && fields.size > 1)
           conflictReferences ++= ownClass._2(fieldName).map(f => (f, fields - f))
      }

      // Add references for conflicting methods
      for ((methodName, methods) <- classEnv._3) {
        if (ownClass._3.contains(methodName) && methods.size > 1)
          conflictReferences ++= ownClass._3(methodName).map(m => (m, methods - m))
      }
    }

    className.resolveNames(nameEnvironment) + superClass.resolveNames(nameEnvironment) +
      elements.foldLeft(NameGraphExtended(Set(), conflictReferences))(_ + _.resolveNames(nameEnvironment, this))
  }

  private var _resolved: NameGraphModular[ClassInterface] = _
  def resolveNamesModular: NameGraphModular[ClassInterface] = {
    if (_resolved != null)
      return _resolved

    var environment: ClassNameEnvironment = Map()

    // Collect all exported super-class fields/methods (if there is a super-class)
    val (superClassFields:Set[Identifier], superClassMethods:Set[Identifier]) = dependencies.find(_.className.name == superClass.name) match {
      case Some(superInterface) => (superInterface.exportedFields, superInterface.exportedMethods)
      case None => (Set(), Set())
    }

    // Merge the fields/methods of the super-class and current class together
    val ownFieldsMap = superClassFields.groupBy(_.name) ++
      fields.map(_.fieldName).groupBy(_.name).map(m => (m._1, superClassFields.groupBy(_.name).getOrElse(m._1, Set()) ++ m._2)).toMap
    val ownMethodsMap = superClassMethods.groupBy(_.name) ++
      methods.map(_.signature.methodName).groupBy(_.name).map(m => (m._1, superClassMethods.groupBy(_.name).getOrElse(m._1, Set()) ++ m._2)).toMap

    // Add the merged sets to the environment
    environment += (className.name -> Set((className, ownFieldsMap, ownMethodsMap)))

    // Add exported fields/methods for all other, external classes to the environment
    for (dependency <- dependencies) {
      if (environment.contains(dependency.moduleID.name))
        throw new IllegalArgumentException("Multiple instances of class '" + dependency.moduleID.name + "' found!")
      else {
        val fieldsMap = dependency.exportedFields.groupBy(_.name)
        val methodsMap = dependency.exportedMethods.groupBy(_.name)
        environment += (dependency.moduleID.name -> Set((dependency.moduleID, fieldsMap, methodsMap)))
      }
    }

    val nameGraph = resolveNames(environment)

    // Create the final modular name graph (and filter remaining, empty edge sets)
    _resolved = NameGraphModular(nameGraph.V, dependencies, nameGraph.E, interface)
    _resolved
  }

  override def toString = "class " + className.toString +
    (if (superClass != ObjectClass) " extends " + superClass.toString else "") + " {\n\t" + elements.mkString("\n\t") + "}"
}
