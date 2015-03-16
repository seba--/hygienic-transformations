package lang.lightweightjava.ast.returnvalue

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.statement.{Null, TermVariable, This}
import name.namegraph.NameGraphExtended
import name.{Name, Identifier, Renaming}

case class ReturnMethodCall(returnObject: TermVariable, methodName: Identifier, methodParameters: TermVariable*) extends ReturnValue {
  require(AST.isLegalName(methodName.name), "Method name '" + methodName + "' is no legal Java method name")

  override def allNames = returnObject.allNames ++ methodParameters.foldLeft(Set[Name]())(_ ++ _.allNames) + methodName.name

  override def rename(renaming: Renaming) = ReturnMethodCall(returnObject.rename(renaming), renaming(methodName), methodParameters.map(_.rename(renaming)): _*)

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment, returnType : ClassRef) = {
    typeEnvironment(returnObject.name) match {
      case className:ClassName =>
        program.findMethod(program.findClassDefinition(className).get, methodName.name) match {
          case Some(method) =>
            require(methodParameters.size == method.signature.parameters.size,
              "Method '" + methodName + "' is called with an invalid number of parameters in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")

            methodParameters.zip(method.signature.parameters).map(param =>
              require(param._1 == Null || program.checkSubclass(typeEnvironment(param._1.name), param._2.variableType),
                "Method '" + methodName + "' is called with an incompatible value for parameter '" + param._2.variableName + "' in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "'"))

            require(className.name == typeEnvironment(This.name).name || method.signature.accessModifier == AccessModifier.PUBLIC,
              "Trying to call private method '" + method.signature.methodName + "' of class '" +
                typeEnvironment(returnObject.name).asInstanceOf[ClassName].name + "' outside visibility range!")

            require(program.checkSubclass(method.signature.returnType, returnType),
              "Method return value returned by a method in class '" +
                typeEnvironment(This.name).asInstanceOf[ClassName].name + "' is incompatible to the method return type!")

            typeEnvironment

          case None =>
            throw new IllegalArgumentException("Class '" + className.name + "' doesn't have method '" + methodName + "' called in class '" +
              typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
        }
      case _ =>
        throw new IllegalArgumentException("Class 'Object' doesn't have method '" + methodName + "' called in class '" +
          typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) = {
    val variablesGraph = returnObject.resolveVariableNames(methodEnvironment) +
      methodParameters.foldLeft(NameGraphExtended(Set(), Map()))(_ + _.resolveVariableNames(methodEnvironment))

    // Two-step lookup: variable name -> class name -> class environment
    if (typeEnvironment.contains(returnObject.name) && nameEnvironment.contains(typeEnvironment(returnObject.name).name)) {
      val classEnv = nameEnvironment(typeEnvironment(returnObject.name).name)
      val methodMap = classEnv.map(_._3).filter(_.contains(methodName.name))

      variablesGraph + NameGraphExtended(Set(methodName), Map(methodName -> methodMap.flatMap(_(methodName.name))))
    }
    else {
      // Return ID without references if lookup fails
      variablesGraph + NameGraphExtended(Set(methodName), Map())
    }
  }

  override def toString = returnObject.toString + "." + methodName.toString + "(" + methodParameters.mkString(", ") + ")"
}