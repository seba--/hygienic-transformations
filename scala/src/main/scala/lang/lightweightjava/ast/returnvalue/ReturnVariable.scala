package lang.lightweightjava.ast.returnvalue

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.statement.{Null, TermVariable, This}
import name.namegraph.NameGraph
import name.Renaming

case class ReturnVariable(variable: TermVariable) extends ReturnValue {
  override def allNames = variable.allNames

  override def rename(renaming: Renaming) = ReturnVariable(variable.rename(renaming))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment, returnType : ClassRef) = {
    require(variable == Null || program.checkSubclass(typeEnvironment(variable), returnType),
      "Variable returned by a method in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "' is incompatible to return type!")
    typeEnvironment
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment: TypeEnvironment): NameGraph =
    variable.resolveVariableNames(methodEnvironment)

  override def toString: String = variable.toString
}