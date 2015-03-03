package lang.lightweightjava.ast

import lang.lightweightjava.ast.statement.{Null, This}
import name.{Identifier, Name, Renaming}

case class MethodDefinition(signature: MethodSignature, methodBody: MethodBody) extends ClassElement {
  override def allNames = signature.allNames ++ methodBody.allNames

  override def rename(renaming: Renaming) = MethodDefinition(signature.rename(renaming), methodBody.rename(renaming))

  private def typeCheckInternal(program: Program, classDefinition : ClassDefinition, typeEnv : TypeEnvironment) = {
    require(signature.parameters.map(_.variableName.name).distinct.size == signature.parameters.size,
      "Parameter names for method definition '" + signature.methodName.name + "' need to be unique")
    require(signature.parameters.map(_.variableType).forall {
      case className:ClassName => program.getClassDefinition(className).isDefined
      case _ => true
    }, "Could not find definition for some method parameter types of method '" + signature.methodName + "' of class '" + classDefinition.className.name + "'")
  }

  def typeCheckForClassDefinition(program: Program, classDefinition : ClassDefinition) = {
    val typeEnv = typeEnvironment(classDefinition)
    typeCheckInternal(program, classDefinition, typeEnv)
    methodBody.statements.foldLeft(typeEnv)((oldEnv, statement) => statement.typeCheckForTypeEnvironment(program, oldEnv))
    methodBody.returnValue.typeCheckForTypeEnvironment(program, typeEnv, signature.returnType)
  }

  def typeEnvironment(classDefinition : ClassDefinition) = {
    signature.parameters.map(param => (param.variableName.name, param.variableType)).toMap[Name, ClassRef] + (This.name -> classDefinition.className)
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = sys.error("Can't resolve names of method definition without class context")

  def resolveNames(nameEnvironment: ClassNameEnvironment, classDefinition : ClassDefinition) = {
    val methodEnvironment = signature.parameters.map(p => (p.variableName.name, p.variableName)).toMap[Name, Identifier] + (This.name -> This) + (Null.name -> Null)

    signature.resolveNames(nameEnvironment) + methodBody.resolveNames(nameEnvironment, methodEnvironment, typeEnvironment(classDefinition))

  }

  override def toString: String = signature.toString + " {\n" + methodBody.toString + "\n\t}\n"
}
