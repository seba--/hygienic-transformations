package lang.lightweightjava.ast

import lang.lightweightjava.ast.statement.VariableName
import name.Renaming
import name.namegraph.NameGraph

case class VariableDeclaration(variableType: ClassRef, name: VariableName) extends AST {
  override def allNames = variableType.allNames ++ name.allNames

  override def rename(renaming: Renaming) = VariableDeclaration(variableType.rename(renaming), name.rename(renaming).asInstanceOf[VariableName])

  def resolveNames(nameEnvironment: ClassNameEnvironment, paramEnvironment: VariableNameEnvironment) = {
    val redefinedVar =
      if (paramEnvironment.contains(name.name)) NameGraph(Set(), Map(name -> paramEnvironment(name.name)))
      else NameGraph(Set(), Map())

    variableType.resolveNames(nameEnvironment) + name.resolveNames(nameEnvironment) + redefinedVar
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = resolveNames(nameEnvironment, Map())


  override def toString = variableType.toString + " " + name.toString
}
