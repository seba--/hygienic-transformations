package lang.lightweightjava.ast

import lang.lightweightjava.ast.AccessModifier._
import name.namegraph.NameGraph
import name.{Identifier, Renaming}

case class MethodSignature(accessModifier: AccessModifier, returnType: ClassRef, methodName: Identifier, parameters: VariableDeclaration*) extends AST {
  require(AST.isLegalName(methodName.name), "Method name '" + methodName + "' is no legal Java method name")

  override def allNames = parameters.foldLeft(Set[Identifier]())(_ ++ _.allNames) ++ returnType.allNames + methodName

  override def rename(renaming: Renaming) =
    MethodSignature(accessModifier, returnType.rename(renaming), renaming(methodName), parameters.map(_.rename(renaming)): _*)

  override def resolveNames(nameEnvironment: ClassNameEnvironment): NameGraph = {
    val methodNameGraph = NameGraph(Set(methodName), Map())
    returnType.resolveNames(nameEnvironment) + methodNameGraph + parameters.foldLeft(NameGraph(Set(), Map()))(_ + _.resolveNames(nameEnvironment))
  }

  override def toString: String = accessModifier.toString + " " + returnType.toString + " " + methodName.toString + "(" + parameters.mkString(", ") + ")"
}
