package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.namegraph.NameGraphExtended
import name.{Name, Renaming}

case class StatementBlock(blockBody: Statement*) extends Statement {
  override def allNames = blockBody.foldLeft(Set[Name]())(_ ++ _.allNames)

  override def rename(renaming: Renaming) = StatementBlock(blockBody.map(_.rename(renaming)): _*)

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    blockBody.foldLeft(typeEnvironment)((oldEnvironment, statement) => statement.typeCheckForTypeEnvironment(program, oldEnvironment))
    typeEnvironment
  }

  // Aggregate the final name graph of the block by resolving each statement with the name environment after the previous ones are evaluated
  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) =
    (blockBody.foldLeft((NameGraphExtended(Set(), Map()), (methodEnvironment, typeEnvironment)))((result, statement) => {
      val statementResult = statement.resolveNames(nameEnvironment, methodEnvironment, typeEnvironment)
      (result._1 + statementResult._1, statementResult._2)
    })._1, (methodEnvironment, typeEnvironment))

  override def toString(preTabs: String) = {
    val innerPreTabs = preTabs + "\t"
    "{\n" + blockBody.foldLeft("")(_ + innerPreTabs + _.toString(innerPreTabs) + "\n") + preTabs + "}"
  }

  override def toString = {
    "{\n\t" + blockBody.foldLeft("")(_ + "\t" + _.toString("\t") + "\n") + "}"
  }
}
