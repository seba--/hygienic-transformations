package lang.lightweightjava.ast.returnvalue

import lang.lightweightjava.ast._
import name.Renaming
import name.namegraph.NameGraphExtended

abstract class ReturnValue extends AST {
  override def rename(renaming: Renaming): ReturnValue

  def typeCheckForTypeEnvironment(program : Program, typeEnvironment : TypeEnvironment, returnType : ClassRef) : TypeEnvironment

  override def resolveNames(nameEnvironment: ClassNameEnvironment) =
    throw new IllegalArgumentException("Can't resolve return value names without method context")

  def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment : VariableNameEnvironment, typeEnvironment : TypeEnvironment) : NameGraphExtended

  def toString(preTabs : String) : String = toString

}