package lang.lightweightjava.ast

import lang.lightweightjava.ast.statement.{Null, TermVariable, This}
import name.{Name, NameGraph}

case class MethodDefinition(signature: MethodSignature, methodBody: MethodBody) extends ClassElement {
  override def allNames = signature.allNames ++ methodBody.allNames

  override def rename(renaming: Renaming) = MethodDefinition(signature.rename(renaming), methodBody.rename(renaming))

  private def typeCheckInternal(program: Program, classDefinition : ClassDefinition, typeEnv : TypeEnvironment) = {
    require(signature.parameters.map(_.name.variableName).distinct.size == signature.parameters.size,
      "Parameter names for method definition '" + signature.methodName + "' need to be unique")
    require(signature.parameters.map(_.variableType).forall {
      case className@ClassName(_) => program.getClassDefinition(className).isDefined
      case _ => true
    }, "Could not find definition for some method parameter types of method '" + signature.methodName + "' of class '" + classDefinition.className.className + "'")
  }

  def typeCheckForClassDefinition(program: Program, classDefinition : ClassDefinition) = {
    val typeEnv = typeEnvironment(classDefinition)
    typeCheckInternal(program, classDefinition, typeEnv)
    methodBody.statements.foldLeft(typeEnv)((oldEnv, statement) => statement.typeCheckForTypeEnvironment(program, oldEnv))
    methodBody.returnValue.typeCheckForTypeEnvironment(program, typeEnv, signature.returnType)
  }

  def typeEnvironment(classDefinition : ClassDefinition): TypeEnvironment = {
    signature.parameters.map(param => (param.name, param.variableType)).toMap[TermVariable, ClassRef] + (This -> classDefinition.className)
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment): NameGraph = sys.error("Can't resolve names of method definition without class context")

  def resolveNames(nameEnvironment: ClassNameEnvironment, classDefinition : ClassDefinition): NameGraph = {
    val methodEnvironment = signature.parameters.map(p => (p.name.variableName, p.name.variableName.id)).toMap[Name, Name.ID] + (This.variableName -> This.variableName.id) + (Null.variableName -> Null.variableName.id)
    signature.resolveNames(nameEnvironment) + methodBody.resolveNames(nameEnvironment, methodEnvironment, typeEnvironment(classDefinition))
  }

  override def toString: String = signature.toString + " {\n" + methodBody.toString + "\n\t}\n"
}
