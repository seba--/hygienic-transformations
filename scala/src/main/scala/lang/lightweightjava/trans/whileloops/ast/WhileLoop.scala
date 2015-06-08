package lang.lightweightjava.trans.whileloops.ast

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.statement.{Null, Statement, TermVariable, This}
import name.Renaming

case class WhileLoop(leftVariable: TermVariable, rightVariable: TermVariable, loopBody: Statement) extends Statement {
  override def allNames = leftVariable.allNames ++ rightVariable.allNames ++ loopBody.allNames

  override def rename(renaming: Renaming) =
    WhileLoop(leftVariable.rename(renaming), rightVariable.rename(renaming), loopBody.rename(renaming))

  override def typeCheckForTypeEnvironment(program : Program, typeEnvironment : TypeEnvironment) = {
    require(leftVariable == Null || rightVariable == Null ||
      program.checkSubclass(typeEnvironment(leftVariable.name), typeEnvironment(rightVariable.name)) ||
      program.checkSubclass(typeEnvironment(rightVariable.name), typeEnvironment(leftVariable.name)),
      "Variables compared in loop header in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "' are incompatible!")
    loopBody.typeCheckForTypeEnvironment(program, typeEnvironment)
    typeEnvironment
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) =
    (leftVariable.resolveVariableNames(methodEnvironment) + rightVariable.resolveVariableNames(methodEnvironment) +
      loopBody.resolveNames(nameEnvironment, methodEnvironment, typeEnvironment)._1, (methodEnvironment, typeEnvironment))

  override def toString(preTabs : String) = {
    val innerPreTabs = preTabs + "\t"
    "while (" + leftVariable.toString + "!=" + rightVariable.toString  + ")\n" + innerPreTabs + loopBody.toString(innerPreTabs)
  }

  override def toString = "while (" + leftVariable.toString + "!=" + rightVariable.toString  + ")\n\t" + loopBody.toString("\t")
}
