package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.Renaming

case class ConditionalBranch(leftVariable: TermVariable, rightVariable: TermVariable, ifBranch: Statement, elseBranch: Statement) extends Statement {
  override def allNames = leftVariable.allNames ++ rightVariable.allNames ++ ifBranch.allNames ++ elseBranch.allNames

  override def rename(renaming: Renaming) =
    ConditionalBranch(leftVariable.rename(renaming), rightVariable.rename(renaming), ifBranch.rename(renaming), elseBranch.rename(renaming))

  override def typeCheckForTypeEnvironment(program : Program, typeEnvironment : TypeEnvironment) = {
    require(leftVariable == Null || rightVariable == Null ||
      program.checkSubclass(typeEnvironment(leftVariable.name), typeEnvironment(rightVariable.name)) ||
      program.checkSubclass(typeEnvironment(rightVariable.name), typeEnvironment(leftVariable.name)),
      "Variables compared in conditional branch in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "' are incompatible!")
    ifBranch.typeCheckForTypeEnvironment(program, typeEnvironment)
    elseBranch.typeCheckForTypeEnvironment(program, typeEnvironment)
    typeEnvironment
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) =
    (leftVariable.resolveVariableNames(methodEnvironment) + rightVariable.resolveVariableNames(methodEnvironment) +
      ifBranch.resolveNames(nameEnvironment, methodEnvironment, typeEnvironment)._1 + elseBranch.resolveNames(nameEnvironment, methodEnvironment, typeEnvironment)._1, (methodEnvironment, typeEnvironment))

  override def toString(preTabs : String) = {
    val innerPreTabs = preTabs + "\t"
    "if (" + leftVariable.toString + " == " + rightVariable.toString  + ")\n" + innerPreTabs + ifBranch.toString(innerPreTabs) + "\n" + preTabs +
      "else\n" + innerPreTabs + elseBranch.toString(innerPreTabs)
  }

  override def toString = "if (" + leftVariable.toString + " == " + rightVariable.toString  + ")\n\t" + ifBranch.toString("\t") + "\n" +
    "else\n\t" + elseBranch.toString("\t")
}
