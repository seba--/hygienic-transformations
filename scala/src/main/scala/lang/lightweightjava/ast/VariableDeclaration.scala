package lang.lightweightjava.ast

import lang.lightweightjava.ast.statement.VariableName
import name.Renaming
import name.namegraph.NameGraph

case class VariableDeclaration(variableType: ClassRef, variableName: VariableName) extends AST {
  override def allNames = variableType.allNames ++ variableName.allNames

  override def rename(renaming: Renaming) = VariableDeclaration(variableType.rename(renaming), variableName.rename(renaming).asInstanceOf[VariableName])

  def resolveNames(nameEnvironment: ClassNameEnvironment, paramEnvironment: VariableNameEnvironment) = {
    val redefinedVar =
      if (paramEnvironment.contains(variableName.name)) NameGraph(Set(), Map(variableName -> paramEnvironment(variableName.name)))
      else NameGraph(Set(), Map())

    variableType.resolveNames(nameEnvironment) + variableName.resolveNames(nameEnvironment) + redefinedVar
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = resolveNames(nameEnvironment, Map())


  override def toString = variableType.toString + " " + variableName.toString
}
