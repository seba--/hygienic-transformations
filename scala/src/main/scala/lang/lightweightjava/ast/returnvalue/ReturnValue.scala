package lang.lightweightjava.ast.returnvalue

import lang.lightweightjava.ast._
import name.NameGraph

abstract class ReturnValue extends AST {
  override def rename(renaming: Renaming): ReturnValue

  def typeCheckForTypeEnvironment(program : Program, typeEnvironment : TypeEnvironment, returnType : ClassRef) : TypeEnvironment

  override def resolveNames(nameEnvironment: ClassNameEnvironment): NameGraph = sys.error("Can't resolve return value names without method context")

  def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment : VariableNameEnvironment, typeEnvironment : TypeEnvironment) : NameGraph

  def toString(preTabs : String) : String = toString

}