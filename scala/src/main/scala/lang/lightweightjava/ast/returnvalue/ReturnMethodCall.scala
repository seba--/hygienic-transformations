package lang.lightweightjava.ast.returnvalue

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.statement.{Null, TermVariable, This}
import name.NameGraph.UnboundReferenceError
import name.{Name, NameGraph}

case class ReturnMethodCall(returnObject: TermVariable, methodName: Name, methodParameters: TermVariable*) extends ReturnValue {
  require(AST.isLegalName(methodName), "Method name '" + methodName + "' is no legal Java method name")

  override def allNames = returnObject.allNames ++ methodParameters.foldLeft(Set[Name.ID]())(_ ++ _.allNames) + methodName.id

  override def rename(renaming: Renaming) = ReturnMethodCall(returnObject.rename(renaming), renaming(methodName), methodParameters.map(_.rename(renaming)): _*)

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment, returnType : ClassRef) = {
    typeEnvironment(returnObject) match {
      case className@ClassName(_) => program.findMethod(program.getClassDefinition(className).get, methodName) match {
        case Some(method) => require(methodParameters.size == method.signature.parameters.size,
          "Method '" + methodName + "' is called with an invalid number of parameters in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
          methodParameters.zip(method.signature.parameters).map(param => require(param._1 == Null || program.checkSubclass(typeEnvironment(param._1), param._2.variableType),
            "Method '" + methodName + "' is called with an incompatible value for parameter '" + param._2.name + "' in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'"))
          require(className.className == typeEnvironment(This).className || method.signature.accessModifier == AccessModifier.PUBLIC, "Trying to call private method '" + method.signature.methodName + "' of class '" + typeEnvironment(returnObject).asInstanceOf[ClassName].className + "' externally!")
          require(program.checkSubclass(method.signature.returnType, returnType),
            "Method return value returned by a method in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "' is incompatible to the method return type!")
          typeEnvironment
        case None =>
          throw new IllegalArgumentException("Class '" + className.className + "' doesn't have method '" + methodName + "' called in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
      }
      case _ => throw new IllegalArgumentException("Class 'Object' doesn't have method '" + methodName + "' called in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment): NameGraph = {
    val variablesGraph = returnObject.resolveVariableNames(methodEnvironment) +
      methodParameters.foldLeft(NameGraph(Set(), Map(), Set()))(_ + _.resolveVariableNames(methodEnvironment))

    // As name resolution doesn't require the program to be type checked, we have to to it here and return an error for unknown methods
    if (typeEnvironment.contains(returnObject) && nameEnvironment.contains(typeEnvironment(returnObject).className)) {
      val methodMap = nameEnvironment(typeEnvironment(returnObject).className)._3
      if (methodMap.contains(methodName))
        variablesGraph + NameGraph(Set((methodName.id, false)), Map(methodName.id -> methodMap(methodName)), Set())
      else
        variablesGraph + NameGraph(Set((methodName.id, false)), Map(), Set(UnboundReferenceError(methodName.id)))
    }
    else {
      variablesGraph + NameGraph(Set((methodName.id, false)), Map(), Set(UnboundReferenceError(methodName.id)))
    }
  }

  override def toString: String = returnObject.toString + "." + methodName.toString + "(" + methodParameters.mkString(", ") + ");"
}