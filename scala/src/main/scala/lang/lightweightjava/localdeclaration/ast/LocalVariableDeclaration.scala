package lang.lightweightjava.localdeclaration.ast

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.statement.{Statement, This, VariableName}
import name.NameGraph

case class LocalVariableDeclaration(variableType: ClassRef, name: VariableName) extends Statement {
  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment: TypeEnvironment): (NameGraph, (VariableNameEnvironment, TypeEnvironment)) =
    (variableType.resolveNames(nameEnvironment) + name.resolveVariableNames(methodEnvironment + (name.variableName -> name.variableName.id)), (methodEnvironment + (name.variableName -> name.variableName.id), typeEnvironment + (name -> variableType)))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment): TypeEnvironment = {
    require(variableType match {
      case className@ClassName(_) => program.getClassDefinition(className).isDefined
      case _ => true
    }, "Could not find definition of type '" + variableType.toString + "' for declaration of variable '" + name.toString + "' in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
    typeEnvironment.get(name) match {
      case Some(ClassName(_)) => sys.error("Variable '" + name.toString + "' in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "' is declared multiple times!")
      case _ => typeEnvironment + (name -> variableType)
    }
  }

  override def allNames = variableType.allNames ++ name.allNames

  override def rename(renaming: Renaming) = LocalVariableDeclaration(variableType.rename(renaming), name.rename(renaming))

  override def toString: String = variableType.toString + " " + name.toString + ";"
}
