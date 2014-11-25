package lang.lightweightjava.ast

import lang.lightweightjava.ast.AccessModifier._
import name.{Name, NameGraph}

case class MethodSignature(accessModifier: AccessModifier, returnType: ClassRef, methodName: Name, parameters: VariableDeclaration*) extends AST {
  require(AST.isLegalName(methodName), "Method name '" + methodName + "' is no legal Java method name")

  override def allNames = parameters.foldLeft(Set[Name.ID]())(_ ++ _.allNames) ++ returnType.allNames + methodName.id

  override def rename(renaming: Renaming) =
    MethodSignature(accessModifier, returnType.rename(renaming), renaming(methodName), parameters.map(_.rename(renaming)): _*)

  override def resolveNames(nameEnvironment: ClassNameEnvironment): NameGraph = {
    val methodNameGraph = accessModifier match {
      case PUBLIC => NameGraph(Set((methodName.id, true)), Map(), Set())
      case PRIVATE => NameGraph(Set((methodName.id, false)), Map(), Set())
    }
    returnType.resolveNames(nameEnvironment) ++ methodNameGraph ++ parameters.foldLeft(NameGraph(Set(), Map(), Set()))(_ ++ _.resolveNames(nameEnvironment))
  }

  override def toString: String = accessModifier.toString + " " + returnType.toString + " " + methodName.toString + "(" + parameters.mkString(", ") + ")"
}
