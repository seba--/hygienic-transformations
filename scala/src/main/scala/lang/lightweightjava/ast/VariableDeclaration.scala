package lang.lightweightjava.ast

import lang.lightweightjava.ast.statement.VariableName
import name.Renaming

case class VariableDeclaration(variableType: ClassRef, name: VariableName) extends AST {
  override def allNames = variableType.allNames ++ name.allNames

  override def rename(renaming: Renaming) = VariableDeclaration(variableType.rename(renaming), name.rename(renaming).asInstanceOf[VariableName])

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = variableType.resolveNames(nameEnvironment) + name.resolveNames(nameEnvironment)

  override def toString = variableType.toString + " " + name.toString
}
