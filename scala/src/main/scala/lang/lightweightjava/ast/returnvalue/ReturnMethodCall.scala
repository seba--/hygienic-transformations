package lang.lightweightjava.ast.returnvalue

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.statement.{Null, TermVariable, This}
import name.namegraph.{NameGraphExtended, NameGraph}
import name.{Identifier, Renaming}

case class ReturnMethodCall(returnObject: TermVariable, methodName: Identifier, methodParameters: TermVariable*) extends ReturnValue {
  require(AST.isLegalName(methodName.name), "Method name '" + methodName + "' is no legal Java method name")

  override def allNames = returnObject.allNames ++ methodParameters.foldLeft(Set[Identifier]())(_ ++ _.allNames) + methodName

  override def rename(renaming: Renaming) = ReturnMethodCall(returnObject.rename(renaming), renaming(methodName), methodParameters.map(_.rename(renaming)): _*)

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment, returnType : ClassRef) = {
    typeEnvironment(returnObject) match {
      case className@ClassName(_) => program.findMethod(program.getClassDefinition(className).get, methodName.name) match {
        case Some(method) => require(methodParameters.size == method.signature.parameters.size,
          "Method '" + methodName + "' is called with an invalid number of parameters in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
          methodParameters.zip(method.signature.parameters).map(param => require(param._1 == Null || program.checkSubclass(typeEnvironment(param._1), param._2.variableType),
            "Method '" + methodName + "' is called with an incompatible value for parameter '" + param._2.name + "' in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'"))
          require(className.name == typeEnvironment(This).name || method.signature.accessModifier == AccessModifier.PUBLIC,
            "Trying to call private method '" + method.signature.methodName + "' of class '" + typeEnvironment(returnObject).asInstanceOf[ClassName].name + "' externally!")
          require(program.checkSubclass(method.signature.returnType, returnType),
            "Method return value returned by a method in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "' is incompatible to the method return type!")
          typeEnvironment
        case None =>
          throw new IllegalArgumentException("Class '" + className.name + "' doesn't have method '" + methodName + "' called in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
      }
      case _ => throw new IllegalArgumentException("Class 'Object' doesn't have method '" + methodName + "' called in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) = {
    val variablesGraph = returnObject.resolveVariableNames(methodEnvironment) +
      methodParameters.foldLeft(NameGraph(Set(), Map()))(_ + _.resolveVariableNames(methodEnvironment))

    if (typeEnvironment.contains(returnObject) && nameEnvironment.contains(typeEnvironment(returnObject).name)) {
      val fieldMap = nameEnvironment(typeEnvironment(returnObject).name).map(_._3).filter(_.contains(methodName.name))

      variablesGraph + NameGraphExtended(Set(methodName), Map(methodName -> fieldMap.flatMap(_(methodName.name))))
    }
    else {
      variablesGraph + NameGraph(Set(methodName), Map())
    }
  }

  override def toString = returnObject.toString + "." + methodName.toString + "(" + methodParameters.mkString(", ") + ");"
}