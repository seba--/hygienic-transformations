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

  override def dependencies: Set[Name] = resolveNamesModular(Set())._1.V.collect({ case className:ClassName => className.name }) - className.name

  override def rename(renaming: Renaming) =
    ClassDefinition(className.rename(renaming).asInstanceOf[ClassName], superClass.rename(renaming), elements.map(_.rename(renaming)): _*)

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
      case className:ClassName => program.findClassDefinition(className).isDefined
      case ObjectClass => true
    }, "Could not find definition for some field types of class '" + className.name + "'")

    program.findAllMethods(this).foreach(_.typeCheckForClassDefinition(program, this))
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = {
    var duplicateReferences: Map[Identifier, Set[Identifier]] = Map()
    val classes = nameEnvironment(className.name)
    if (classes.size > 1)
      duplicateReferences += (className -> (classes.map(_._1.asInstanceOf[Identifier]) - className))

    val ownClass = classes.find(_._1 == className).get
    for (classEnv <- classes) {
      for ((fieldName, fields) <- classEnv._2) {
         if (ownClass._2.contains(fieldName) && fields.size > 1)
           duplicateReferences ++= ownClass._2(fieldName).map(f => (f, fields - f))
      }

      for ((methodName, methods) <- classEnv._3) {
        if (ownClass._3.contains(methodName) && methods.size > 1)
          duplicateReferences ++= ownClass._3(methodName).map(m => (m, methods - m))
      }
    }

    className.resolveNames(nameEnvironment) + superClass.resolveNames(nameEnvironment) +
      elements.foldLeft(NameGraphExtended(Set(), duplicateReferences))(_ + _.resolveNames(nameEnvironment, this))
  }

  override def resolveNamesModular(metaDependencies: Set[ClassInterface] = Set()): (NameGraphModular, ClassInterface) = {
    val classInterface = ClassInterface(className, exportedFields, exportedMethods)
    var environment: ClassNameEnvironment = Map()

    val (superClassFields:Set[Identifier], superClassMethods:Set[Identifier]) = metaDependencies.find(_.className.name == superClass.name) match {
      case Some(superInterface) => (superInterface.exportedFields, superInterface.exportedMethods)
      case None => (Set(), Set())
    }

    // Merging the mappings of super class and current class together
    val ownFieldsMap = superClassFields.groupBy(_.name) ++
      fields.map(_.fieldName).groupBy(_.name).map(m => (m._1, superClassFields.groupBy(_.name).getOrElse(m._1, Set()) ++ m._2)).toMap
    val ownMethodsMap = superClassMethods.groupBy(_.name) ++
      methods.map(_.signature.methodName).groupBy(_.name).map(m => (m._1, superClassMethods.groupBy(_.name).getOrElse(m._1, Set()) ++ m._2)).toMap

    environment += (className.name -> Set((className, ownFieldsMap, ownMethodsMap)))

    for (dependency <- metaDependencies) {
      if (environment.contains(dependency.moduleID.name))
        throw new IllegalArgumentException("Multiple instances of class '" + dependency.moduleID.name + "' found!")
      else {
        val fieldsMap = dependency.exportedFields.groupBy(_.name)
        val methodsMap = dependency.exportedMethods.groupBy(_.name)
        environment += (dependency.moduleID.name -> Set((dependency.moduleID, fieldsMap, methodsMap)))
      }
    }

    val nameGraph = resolveNames(environment)
    var intEdges: Map[Identifier, Set[Identifier]] = Map()
    var outEdges: Map[Identifier, Set[Identifier]] = Map()

    for ((v, ds) <- nameGraph.E.filter(e => nameGraph.V.contains(e._1))) {
      intEdges += (v -> ds.filter(d => nameGraph.V.contains(d)))
      outEdges += (v -> ds.filter(d => !nameGraph.V.contains(d)))
    }

    val modularNameGraph = NameGraphModular(nameGraph.V, intEdges.filter(_._2.size > 0), outEdges.filter(_._2.size > 0))

    (modularNameGraph, classInterface)
  }

  override def resolveNamesVirtual(metaDependencies: Set[ClassInterface], renaming: Renaming): NameGraphModular = {
    val dependenciesRenamed = metaDependencies.map(i =>
      ClassInterface(i.moduleID, i.exportedFields.map(f => renaming(f)), i.exportedMethods.map(m => renaming(m))))
    resolveNamesModular(dependenciesRenamed)._1
  }

  override def toString = "class " + className.toString +
    (if (superClass != ObjectClass) " extends " + superClass.toString else "") + " {\n\t" + elements.mkString("\n\t") + "}"
}
