package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.namegraph.NameGraphExtended
import name.{Identifier, Renaming}

case class VoidMethodCall(sourceObject: TermVariable, methodName: Identifier, methodParameters: TermVariable*) extends Statement {
  require(AST.isLegalName(methodName.name), "Method name '" + methodName + "' is no legal Java method name")

  override def allNames = sourceObject.allNames ++ methodParameters.foldLeft(Set[Identifier]())(_ ++ _.allNames) + methodName

  override def rename(renaming: Renaming) =
    VoidMethodCall(sourceObject.rename(renaming), renaming(methodName), methodParameters.map(_.rename(renaming)): _*)

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    typeEnvironment(sourceObject.name) match {
      case className:ClassName => program.findMethod(program.getClassDefinition(className).get, methodName.name) match {
        case Some(method) => require(methodParameters.size == method.signature.parameters.size,
          "Method '" + methodName.name + "' is called with an invalid number of parameters in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
          methodParameters.zip(method.signature.parameters).map(param => require(param._1 == Null || program.checkSubclass(typeEnvironment(param._1.name), param._2.variableType),
            "Method '" + methodName.name + "' is called with an incompatible value for parameter '" + param._2.name + "' in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "'"))
          require(className.name == typeEnvironment(This.name).name || method.signature.accessModifier == AccessModifier.PUBLIC,
            "Trying to call private method '" + method.signature.methodName + "' of class '" + typeEnvironment(sourceObject.name).asInstanceOf[ClassName].name + "' externally!")
          typeEnvironment
        case None =>
          throw new IllegalArgumentException("Class '" + className.name + "' doesn't have method '" + methodName + "' called in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
      }
      case _ => throw new IllegalArgumentException("Class 'Object' doesn't have method '" + methodName + "' called in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) = {
    val variablesGraph = sourceObject.resolveVariableNames(methodEnvironment) +
      methodParameters.foldLeft(NameGraphExtended(Set(), Map()))(_ + _.resolveVariableNames(methodEnvironment))

    if (typeEnvironment.contains(sourceObject.name) && nameEnvironment.contains(typeEnvironment(sourceObject.name).name)) {
      val fieldMap = nameEnvironment(typeEnvironment(sourceObject.name).name).map(_._3).filter(_.contains(methodName.name))

      (variablesGraph + NameGraphExtended(Set(methodName), Map(methodName -> fieldMap.flatMap(_(methodName.name)))), (methodEnvironment, typeEnvironment))
    }
    else {
      (variablesGraph + NameGraphExtended(Set(methodName), Map()), (methodEnvironment, typeEnvironment))
    }
  }

  override def toString = sourceObject.toString + "." + methodName.toString + "(" + methodParameters.mkString(", ") + ");"
}
