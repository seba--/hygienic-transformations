package lang.lightweightjava.ast

import lang.lightweightjava.ast.returnvalue.ReturnValue
import lang.lightweightjava.ast.statement.Statement
import name.namegraph.{NameGraphExtended, NameGraph}
import name.{Identifier, Renaming}

case class MethodBody(returnValue: ReturnValue, statements: Statement*) extends AST {
  override def allNames = statements.foldLeft(Set[Identifier]())(_ ++ _.allNames) ++ returnValue.allNames

  override def rename(renaming: Renaming) = MethodBody(returnValue.rename(renaming), statements.map(_.rename(renaming)): _*)

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = sys.error("Can't resolve method body names without method context")

  def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment : VariableNameEnvironment, typeEnvironment : TypeEnvironment) : NameGraphExtended = {
    val methodBodyResult = statements.foldLeft((NameGraph(Set(), Map()), (methodEnvironment, typeEnvironment)))((result, statement) => {
      val statementResult = statement.resolveNames(nameEnvironment, result._2._1, result._2._2)
      (result._1 + statementResult._1, statementResult._2)
    })
    returnValue.resolveNames(nameEnvironment, methodBodyResult._2._1, methodBodyResult._2._2) + methodBodyResult._1
  }

  override def toString: String = statements.foldLeft("")(_ + "\t\t" + _.toString("\t\t") + "\n") + "\t\treturn " + returnValue.toString() + ";"
}
