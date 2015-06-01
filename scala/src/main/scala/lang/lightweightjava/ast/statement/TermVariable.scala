package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.namegraph.{NameGraphExtended, NameGraph}
import name.{Identifier, Name, Renaming}

import scala.language.implicitConversions

trait TermVariable extends Identifier with AST {
  override def allNames = Set()

  protected def renameVariable(newName : Name) = {
    if (newName == "this") This
    else if (newName == "null") Null
    else {
      val renamed = VariableName(newName)
      renamed.id = this.id
      renamed.oName = this.oName
      renamed
    }
  }

  override def rename(renaming: Renaming): TermVariable = renameVariable(renaming(this).name)

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = NameGraph(Set(), Map())

  def resolveVariableNames(methodEnvironment : VariableNameEnvironment): NameGraphExtended = NameGraph(Set(), Map())
}

object This extends Identifier("this") with TermVariable {
  override def fresh = throw new IllegalArgumentException("Can't create fresh instance of 'this' variable!")
}

object Null extends Identifier("null") with TermVariable {
  override def fresh = throw new IllegalArgumentException("Can't create fresh instance of 'null' variable!")
}

object VariableName {
  implicit def apply(name: Name): VariableName = {
    new VariableName(name)
  }
}

class VariableName(override val name: Name) extends Identifier(name) with TermVariable {
  require(AST.isLegalName(name), "Variable name '" + name + "' is no legal Java variable name")

  override def allNames = Set(this.name)

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = resolveVariableNames(Map(name -> this))

  override def resolveVariableNames(methodEnvironment : VariableNameEnvironment) = {
    // If the variable is pointing to itself (because it is declared here), add only the node but no edges
    if (!methodEnvironment.contains(name) || methodEnvironment(name) == this)
      NameGraphExtended(Set(this), Map())
    // If the variable is pointing to another variable, add it and the edge to the name graph
    else
      NameGraphExtended(Set(this), Map(this -> Set(methodEnvironment(name))))
  }

  override def fresh = VariableName(name)
}


