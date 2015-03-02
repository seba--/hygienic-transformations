package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.namegraph.NameGraph
import name.{Identifier, Renaming}

case class StatementBlock(blockBody: Statement*) extends Statement {
  override def allNames = blockBody.foldLeft(Set[Identifier]())(_ ++ _.allNames)

  override def rename(renaming: Renaming) = StatementBlock(blockBody.map(_.rename(renaming)): _*)

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    blockBody.foldLeft(typeEnvironment)((oldEnvironment, statement) => statement.typeCheckForTypeEnvironment(program, oldEnvironment))
    typeEnvironment
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) =
    (blockBody.foldLeft((NameGraph(Set(), Map()), (methodEnvironment, typeEnvironment)))((result, statement) => {
      val statementResult = statement.resolveNames(nameEnvironment, methodEnvironment, typeEnvironment)
      (result._1 + statementResult._1, statementResult._2)
    })._1, (methodEnvironment, typeEnvironment))

  override def toString(preTabs: String): String = {
    val innerPreTabs = preTabs + "\t"
    "{\n" + blockBody.foldLeft("")(_ + innerPreTabs + _.toString(innerPreTabs) + "\n") + preTabs + "}"
  }

  override def toString: String = {
    "{\n\t" + blockBody.foldLeft("")(_ + "\t" + _.toString("\t") + "\n") + "}"
  }
}
