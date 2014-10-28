package lang.lightweightjava.localdeclaration

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.returnvalue.ReturnMethodCall
import lang.lightweightjava.ast.statement._
import lang.lightweightjava.localdeclaration.ast.LocalVariableDeclaration
import name.Name


object LocalDeclarationTransformation {
  def transform(program : Program) = {
    Program(program.classes.map(transformClass):_*)
  }

  private def transformClass(classDefinition : ClassDefinition) = {
    classDefinition.elements.collect({ case method@MethodDefinition(_, _) => method}).foldLeft(classDefinition)(transformMethod)
  }

  private def transformMethod(classDefinition : ClassDefinition, method : MethodDefinition) = {
    var newMethodParameters = Seq[VariableDeclaration]()
    var variableMappings = Map[Name, Name]()
    def variableRenaming = (name : Name) => variableMappings.getOrElse(name, name)
    val newMethodBody = method.methodBody.statements.map {
      case LocalVariableDeclaration(variableType, name) =>
        val lvdName = newMethodParameters.find(_.name.variableName == name.variableName) match {
          case Some(param) => param.name.variableName
          case None =>
            newMethodParameters = newMethodParameters :+ VariableDeclaration(variableType, VariableName(name.variableName.fresh))
            name.variableName
        }
        variableMappings = variableMappings + (name.variableName -> lvdName.fresh)
        VariableAssignment(VariableName(lvdName.fresh), Null)
      case statement => statement.rename(variableRenaming).asInstanceOf[statement.type]
    }
    if (newMethodParameters.isEmpty) classDefinition
    else {
      val newMethodSignature = MethodSignature(AccessModifier.PRIVATE, method.signature.returnType, Name(method.signature.methodName.name + "_ldt"),
        method.signature.parameters.map(param => VariableDeclaration(param.variableType, VariableName(param.name.variableName.fresh))) ++ newMethodParameters:_*)
      val newMethod = MethodDefinition(newMethodSignature, MethodBody(method.methodBody.returnValue.rename(variableRenaming), newMethodBody:_*))

      val replacedOldMethod = MethodDefinition(method.signature, MethodBody(ReturnMethodCall(This, Name(method.signature.methodName.name + "_ldt"),
        method.signature.parameters.map(param => param.name) ++ newMethodParameters.map(_ => Null):_*)))
      val replacedElements = classDefinition.elements.map(element => if (element == method) replacedOldMethod else element)
      ClassDefinition(classDefinition.className, classDefinition.superClass, replacedElements :+ newMethod:_*)
    }

  }
}
