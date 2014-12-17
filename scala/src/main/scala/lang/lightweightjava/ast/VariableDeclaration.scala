package lang.lightweightjava.ast

import lang.lightweightjava.ast.statement.VariableName
import name.NameGraph

case class VariableDeclaration(variableType: ClassRef, name: VariableName) extends AST {
  override def allNames = variableType.allNames ++ name.allNames

  override def rename(renaming: RenamingFunction) = VariableDeclaration(variableType.rename(renaming), name.rename(renaming))

  override def resolveNames(nameEnvironment: ClassNameEnvironment): NameGraph = variableType.resolveNames(nameEnvironment) ++ name.resolveNames(nameEnvironment)

  override def toString: String = variableType.toString + " " + name.toString
}
