package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.NameGraph._
import name.{Name, NameGraph}

case class VoidMethodCall(sourceObject: TermVariable, methodName: Name, methodParameters: TermVariable*) extends Statement {
  require(AST.isLegalName(methodName), "Method name '" + methodName + "' is no legal Java method name")

  override def allNames = sourceObject.allNames ++ methodParameters.foldLeft(Set[Name.ID]())(_ ++ _.allNames) + methodName.id

  override def rename(renaming: Renaming) =
    VoidMethodCall(sourceObject.rename(renaming), renaming(methodName), methodParameters.map(_.rename(renaming)): _*)

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    typeEnvironment(sourceObject) match {
      case className@ClassName(_) => program.findMethod(program.getClassDefinition(className).get, methodName) match {
        case Some(method) => require(methodParameters.size == method.signature.parameters.size,
          "Method '" + methodName + "' is called with an invalid number of parameters in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
          methodParameters.zip(method.signature.parameters).map(param => require(param._1 == Null || program.checkSubclass(typeEnvironment(param._1), param._2.variableType),
            "Method '" + methodName + "' is called with an incompatible value for parameter '" + param._2.name + "' in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'"))
          require(className.className == typeEnvironment(This).className || method.signature.accessModifier == AccessModifier.PUBLIC, "Trying to call private method '" + method.signature.methodName + "' of class '" + typeEnvironment(sourceObject).asInstanceOf[ClassName].className + "' externally!")
          typeEnvironment
        case None =>
          throw new IllegalArgumentException("Class '" + className.className + "' doesn't have method '" + methodName + "' called in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
      }
      case _ => throw new IllegalArgumentException("Class 'Object' doesn't have method '" + methodName + "' called in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) = {
    val variablesGraph = sourceObject.resolveVariableNames(methodEnvironment) +
      methodParameters.foldLeft(NameGraph(Set(), Map(), Set()))(_ + _.resolveVariableNames(methodEnvironment))

    // As name resolution doesn't require the program to be type checked, we have to to it here and return an error for unknown methods
    if (typeEnvironment.contains(sourceObject) && nameEnvironment.contains(typeEnvironment(sourceObject).className)) {
      val methodMap = nameEnvironment(typeEnvironment(sourceObject).className)._3
      if (methodMap.contains(methodName))
        (variablesGraph + NameGraph(Set(methodName.id), Map(methodName.id -> methodMap(methodName)), Set()), (methodEnvironment, typeEnvironment))
      else
        (variablesGraph + NameGraph(Set(methodName.id), Map(), Set(UnboundReferenceError(methodName.id))), (methodEnvironment, typeEnvironment))
    }
    else {
      (variablesGraph + NameGraph(Set(methodName.id), Map(), Set(UnboundReferenceError(methodName.id))), (methodEnvironment, typeEnvironment))
    }
  }

  override def toString: String = sourceObject.toString + "." + methodName.toString + "(" + methodParameters.mkString(", ") + ");"
}
