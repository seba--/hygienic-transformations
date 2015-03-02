package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.namegraph.NameGraph
import name.{Identifier, Name, Renaming}

trait TermVariable extends Identifier with AST {
  override def allNames = Set()

  protected def renameVariable(newName : Name) = {
    if (newName == "this") This
    else if (newName == "null") Null
    else {
      val renamed = VariableName(newName)
      renamed.id = this.id
      renamed
    }
  }

  override def rename(renaming: Renaming): TermVariable = renameVariable(renaming(this).name)

  override def resolveNames(nameEnvironment: ClassNameEnvironment): NameGraph = NameGraph(Set(), Map())

  def resolveVariableNames(methodEnvironment : VariableNameEnvironment): NameGraph = NameGraph(Set(), Map())

  override def toString: String = name.toString
}

object This extends Identifier("this") with TermVariable {
  override def fresh = throw new IllegalArgumentException("Can't create fresh instance of 'this' variable!")
}

object Null extends Identifier("null") with TermVariable {
  override def fresh = throw new IllegalArgumentException("Can't create fresh instance of 'null' variable!")
}

case class VariableName(override val name: Name) extends Identifier(name) with TermVariable {
  require(AST.isLegalName(name), "Variable name '" + name + "' is no legal Java variable name")

  override def allNames = Set(this)

  override def resolveNames(nameEnvironment: ClassNameEnvironment): NameGraph = resolveVariableNames(Map(name -> this))

  override def resolveVariableNames(methodEnvironment : VariableNameEnvironment): NameGraph = {
    // If the variable is pointing to itself (because it is declared here), add only the node but no edges
    if (!methodEnvironment.contains(name) || methodEnvironment(name) == this)
      NameGraph(Set(this), Map())
    // If the variable is pointing to another variable, add it and the edge to the name graph
    else
      NameGraph(Set(this), Map(this -> methodEnvironment(name)))
  }

  override def fresh = VariableName(name)
}


