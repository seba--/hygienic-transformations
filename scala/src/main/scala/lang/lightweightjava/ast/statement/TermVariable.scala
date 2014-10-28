package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.NameGraph._
import name.{Name, NameGraph}

abstract class TermVariable extends AST {
  override def rename(renaming: Renaming): TermVariable

  def variableName : Name

  override def resolveNames(nameEnvironment: ClassNameEnvironment): NameGraph = resolveVariableNames(Map(variableName -> variableName.id))

  def resolveVariableNames(methodEnvironment : VariableNameEnvironment): NameGraph = {
    // If the variable isn't in the environment, add an error to the name graph
    if (!methodEnvironment.contains(variableName))
      NameGraph(Set(variableName.id), Map(), Set(UnboundReferenceError(variableName.id)))
    // If the variable is pointing to itself (because it is declared here), add only the node but no edges
    else if (methodEnvironment(variableName) == variableName.id)
      NameGraph(Set(variableName.id), Map(), Set())
    // If the variable is pointing to another variable, add it and the edge to the name graph
    else
      NameGraph(Set(variableName.id), Map(variableName.id -> methodEnvironment(variableName)), Set())
  }

  override def toString: String = variableName.toString
}

object This extends TermVariable {
  private val thisName = Name("this")

  override def variableName = thisName

  override def allNames = Set(variableName.id)

  override def rename(renaming: Renaming) = if (renaming(variableName).name == "this") this else VariableName(renaming(variableName))
}

case class VariableName(variableName: Name) extends TermVariable {
  require(AST.isLegalName(variableName), "Variable name '" + variableName + "' is no legal Java variable name")

  override def allNames = Set(variableName.id)

  override def rename(renaming: Renaming) = VariableName(renaming(variableName))
}

object Null extends TermVariable {
  private val nullName = Name("null")

  override def variableName = nullName

  override def allNames = Set(variableName.id)

  override def rename(renaming: Renaming) = this
}


