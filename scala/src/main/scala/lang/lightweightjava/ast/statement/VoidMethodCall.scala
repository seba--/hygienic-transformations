package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.namegraph.NameGraph
import name.{Identifier, Renaming}

case class VoidMethodCall(sourceObject: TermVariable, methodName: Identifier, methodParameters: TermVariable*) extends Statement {
  require(AST.isLegalName(methodName.name), "Method name '" + methodName + "' is no legal Java method name")

  override def allNames = sourceObject.allNames ++ methodParameters.foldLeft(Set[Identifier]())(_ ++ _.allNames) + methodName

  override def rename(renaming: Renaming) =
    VoidMethodCall(sourceObject.rename(renaming), renaming(methodName), methodParameters.map(_.rename(renaming)): _*)

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    typeEnvironment(sourceObject) match {
      case className@ClassName(_) => program.findMethod(program.getClassDefinition(className).get, methodName.name) match {
        case Some(method) => require(methodParameters.size == method.signature.parameters.size,
          "Method '" + methodName.name + "' is called with an invalid number of parameters in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
          methodParameters.zip(method.signature.parameters).map(param => require(param._1 == Null || program.checkSubclass(typeEnvironment(param._1), param._2.variableType),
            "Method '" + methodName.name + "' is called with an incompatible value for parameter '" + param._2.name + "' in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'"))
          require(className.name == typeEnvironment(This).name || method.signature.accessModifier == AccessModifier.PUBLIC,
            "Trying to call private method '" + method.signature.methodName + "' of class '" + typeEnvironment(sourceObject).asInstanceOf[ClassName].name + "' externally!")
          typeEnvironment
        case None =>
          throw new IllegalArgumentException("Class '" + className.name + "' doesn't have method '" + methodName + "' called in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
      }
      case _ => throw new IllegalArgumentException("Class 'Object' doesn't have method '" + methodName + "' called in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) = {
    val variablesGraph = sourceObject.resolveVariableNames(methodEnvironment) +
      methodParameters.foldLeft(NameGraph(Set(), Map()))(_ + _.resolveVariableNames(methodEnvironment))

    // As name resolution doesn't require the program to be type checked, we have to to it here and return an error for unknown methods
    if (typeEnvironment.contains(sourceObject) && nameEnvironment.contains(typeEnvironment(sourceObject).name)) {
      val methodMap = nameEnvironment(typeEnvironment(sourceObject).name)._3
      if (methodMap.contains(methodName.name))
        (variablesGraph + NameGraph(Set(methodName), Map(methodName -> methodMap(methodName.name))), (methodEnvironment, typeEnvironment))
      else
        (variablesGraph + NameGraph(Set(methodName), Map()), (methodEnvironment, typeEnvironment))
    }
    else {
      (variablesGraph + NameGraph(Set(methodName), Map()), (methodEnvironment, typeEnvironment))
    }
  }

  override def toString: String = sourceObject.toString + "." + methodName.toString + "(" + methodParameters.mkString(", ") + ");"
}
