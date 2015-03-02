package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.Renaming

case class VariableAssignment(target: VariableName, source: TermVariable) extends Statement {
  override def allNames = target.allNames ++ source.allNames

  override def rename(renaming: Renaming) = VariableAssignment(target.rename(renaming).asInstanceOf[VariableName], source.rename(renaming))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    require(source == Null || program.checkSubclass(typeEnvironment(source), typeEnvironment(target)),
      "Variables assigned in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "' are incompatible!")
    typeEnvironment
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment: TypeEnvironment) =
    (target.resolveVariableNames(methodEnvironment) + source.resolveVariableNames(methodEnvironment), (methodEnvironment, typeEnvironment))

  override def toString = target.toString + " = " + source.toString + ";"
}