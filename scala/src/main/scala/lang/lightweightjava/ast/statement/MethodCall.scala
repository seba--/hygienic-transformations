package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.namegraph.NameGraphExtended
import name.{Name, Identifier, Renaming}

case class MethodCall(target: VariableName, sourceObject: TermVariable, methodName: Identifier, methodParameters: TermVariable*) extends Statement {
  require(AST.isLegalName(methodName.name), "Method name '" + methodName + "' is no legal Java method name")

  override def allNames = target.allNames ++ sourceObject.allNames ++ methodParameters.foldLeft(Set[Name]())(_ ++ _.allNames) + methodName.name

  override def rename(renaming: Renaming) =
    MethodCall(target.rename(renaming).asInstanceOf[VariableName], sourceObject.rename(renaming), renaming(methodName), methodParameters.map(_.rename(renaming)): _*)

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    typeEnvironment(sourceObject.name) match {
      case className:ClassName =>
        program.findMethod(program.findClassDefinition(className).get, methodName.name) match {
          case Some(method) =>
            require(methodParameters.size == method.signature.parameters.size,
              "Method '" + methodName + "' is called with an invalid number of parameters in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")

            methodParameters.zip(method.signature.parameters).map(param =>
              require(param._1 == Null || program.checkSubclass(typeEnvironment(param._1.name), param._2.variableType),
                "Method '" + methodName + "' is called with an incompatible value for parameter '" + param._2.variableName + "' in class '" +
                  typeEnvironment(This.name).asInstanceOf[ClassName].name + "'"))

            require(className.name == typeEnvironment(This.name).name || method.signature.accessModifier == AccessModifier.PUBLIC,
              "Trying to call private method '" + method.signature.methodName + "' of class '" +
                typeEnvironment(sourceObject.name).asInstanceOf[ClassName].name + "' externally!")

            require(program.checkSubclass(method.signature.returnType, typeEnvironment(target.name)),
              "Variable and the method return value it is assigned in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "' are incompatible!")

            typeEnvironment

          case None =>
            throw new IllegalArgumentException("Class '" + className.name + "' doesn't have method '" + methodName + "' called in class '" +
              typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
        }
      case _ => throw new IllegalArgumentException("Class 'Object' doesn't have method '" + methodName + "' called in class '" +
        typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) = {
    val variablesGraph = target.resolveVariableNames(methodEnvironment) + sourceObject.resolveVariableNames(methodEnvironment) +
      methodParameters.foldLeft(NameGraphExtended(Set(), Map()))(_ + _.resolveVariableNames(methodEnvironment))

    // Two-step lookup: variable name -> class name -> class environment
    if (typeEnvironment.contains(sourceObject.name) && nameEnvironment.contains(typeEnvironment(sourceObject.name).name)) {
      val classEnv = nameEnvironment(typeEnvironment(sourceObject.name).name)
      val fieldMap = classEnv.map(_._3).filter(_.contains(methodName.name))

      (variablesGraph + NameGraphExtended(Set(methodName), Map(methodName -> fieldMap.flatMap(_(methodName.name)))), (methodEnvironment, typeEnvironment))
    }
    else {
      // Return ID without references if lookup fails
      (variablesGraph + NameGraphExtended(Set(methodName), Map()), (methodEnvironment, typeEnvironment))
    }
  }

  override def toString = target.toString + " = " + sourceObject.toString + "." + methodName.toString + "(" + methodParameters.mkString(", ") + ");"
}
