package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.Renaming
import name.namegraph.NameGraphExtended

abstract class Statement extends AST {
  override def rename(renaming: Renaming): Statement

  def typeCheckForTypeEnvironment(program : Program, typeEnvironment : TypeEnvironment) : TypeEnvironment

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = sys.error("Can't resolve statement names without method context")

  def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment : VariableNameEnvironment, typeEnvironment : TypeEnvironment) : (NameGraphExtended, (VariableNameEnvironment, TypeEnvironment))

  def toString(preTabs : String) : String = toString
}
