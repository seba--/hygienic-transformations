package lang.lightweightjava.localdeclaration

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.returnvalue.{ReturnValue, ReturnMethodCall}
import lang.lightweightjava.ast.statement._
import lang.lightweightjava.localdeclaration.ast.LocalVariableDeclaration
import name.{Identifier, Name}


object LocalDeclarationTransformation {
  def transform(program : Program) = {
    Program(program.classes.map(transformClass):_*)
  }

  private def transformClass(classDefinition : ClassDefinition) = {
    classDefinition.elements.collect({ case method@MethodDefinition(_, _) => method}).foldLeft(classDefinition)(transformMethod)
  }

  private def transformMethod(classDefinition : ClassDefinition, method : MethodDefinition) = {
    var newMethodParameters = Seq[VariableDeclaration]()
    var variableMappings = Map[Identifier, Name]()
    val newMethodBody = method.methodBody.statements.map {
      case LocalVariableDeclaration(variableType, name) =>
        val lvdName = newMethodParameters.find(_.variableName.name == name.name) match {
          case Some(param) => param.variableName
          case None =>
            newMethodParameters = newMethodParameters :+ VariableDeclaration(variableType, name.fresh)
            name
        }
        variableMappings = variableMappings + (name -> lvdName.name)
        VariableAssignment(lvdName.fresh, Null)
      case statement => statement.rename(variableMappings).asInstanceOf[statement.type]
    }
    if (newMethodParameters.isEmpty) classDefinition
    else {
      val newMethodSignature = MethodSignature(AccessModifier.PRIVATE, method.signature.returnType, Identifier(method.signature.methodName.name + "_ldt"),
        method.signature.parameters.map(param => VariableDeclaration(param.variableType, param.variableName.fresh)) ++ newMethodParameters:_*)
      val newMethod = MethodDefinition(newMethodSignature, MethodBody(method.methodBody.returnValue.rename(variableMappings).asInstanceOf[ReturnValue], newMethodBody:_*))

      val replacedOldMethod = MethodDefinition(method.signature, MethodBody(ReturnMethodCall(This, Identifier(method.signature.methodName.name + "_ldt"),
        method.signature.parameters.map(param => param.variableName) ++ newMethodParameters.map(_ => Null):_*)))
      val replacedElements = classDefinition.elements.map(element => if (element == method) replacedOldMethod else element)
      ClassDefinition(classDefinition.className, classDefinition.superClass, replacedElements :+ newMethod:_*)
    }

  }
}
