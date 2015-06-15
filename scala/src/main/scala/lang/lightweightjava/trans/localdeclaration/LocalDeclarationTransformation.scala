package lang.lightweightjava.trans.localdeclaration

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.returnvalue.{ReturnValue, ReturnMethodCall}
import lang.lightweightjava.ast.statement._
import lang.lightweightjava.trans.localdeclaration.ast.LocalVariableDeclaration
import name.{Identifier, Name}


object LocalDeclarationTransformation {
  def transform(program : Program, useAccessModifiers: Boolean = true) = {
    Program(program.classes.map(c => transformClass(c, useAccessModifiers)):_*)
  }

  def transformClass(classDefinition : ClassDefinition, useAccessModifiers: Boolean = true) = {
    classDefinition.methods.foldLeft(classDefinition)((cd, next) => transformMethod(cd, next, useAccessModifiers))
  }

  private def transformMethod(classDefinition : ClassDefinition, method : MethodDefinition, useAccessModifiers: Boolean) = {
    var newMethodParameters = Seq[VariableDeclaration]()
    var variableMappings = Map[Identifier, Name]()
    val newMethodBody = method.methodBody.statements.map {
      case LocalVariableDeclaration(variableType, name) =>
        val lvdName = newMethodParameters.find(_.variableName.name == name.name) match {
          case Some(param) => param.variableName
          case None =>
            newMethodParameters = newMethodParameters :+ VariableDeclaration(variableType, name)
            name
        }
        variableMappings = variableMappings + (name -> lvdName.name)
        VariableAssignment(lvdName, Null)
      case statement => statement.rename(variableMappings).asInstanceOf[statement.type]
    }
    if (newMethodParameters.isEmpty) classDefinition
    else {
      val accessModifier =
        if (useAccessModifiers) AccessModifier.PRIVATE
        else AccessModifier.PUBLIC

      val newMethodSignature = MethodSignature(accessModifier, method.signature.returnType, Identifier(method.signature.methodName.name + "Helper"),
        method.signature.parameters.map(param => VariableDeclaration(param.variableType, param.variableName)) ++ newMethodParameters:_*)
      val newMethod = MethodDefinition(newMethodSignature, MethodBody(method.methodBody.returnValue.rename(variableMappings).asInstanceOf[ReturnValue], newMethodBody:_*))

      val replacedOldMethod = MethodDefinition(method.signature, MethodBody(ReturnMethodCall(This, Identifier(method.signature.methodName.name + "Helper"),
        method.signature.parameters.map(param => param.variableName) ++ newMethodParameters.map(_ => Null):_*)))
      val replacedElements = classDefinition.elements.map(element =>
        if (element == method)
          replacedOldMethod
        else
          element)

      ClassDefinition(classDefinition.className, classDefinition.superClass, replacedElements :+ newMethod:_*)
    }

  }
}
