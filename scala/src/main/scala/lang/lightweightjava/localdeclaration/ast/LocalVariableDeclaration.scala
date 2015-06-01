package lang.lightweightjava.localdeclaration.ast

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.statement.{Statement, This, VariableName}
import name.Renaming
import name.namegraph.NameGraph

case class LocalVariableDeclaration(variableType: ClassRef, variableName: VariableName) extends Statement {
  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment: TypeEnvironment) = {
    val redefinedVar =
      if (methodEnvironment.contains(variableName.name)) NameGraph(Set(), Map(variableName -> methodEnvironment(variableName.name)))
      else NameGraph(Set(), Map())

    (variableType.resolveNames(nameEnvironment) + variableName.resolveVariableNames(methodEnvironment + (variableName.name -> variableName)) + redefinedVar,
      (methodEnvironment + (variableName.name -> variableName), typeEnvironment + (variableName.name -> variableType)))
  }

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    require(variableType match {
      case className:ClassName => program.findClassDefinition(className).isDefined
      case _ => true
    }, "Could not find definition of type '" + variableType.toString + "' for declaration of variable '" + variableName.toString + "' in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
    typeEnvironment.get(variableName.name) match {
      case Some(_:ClassName) => sys.error("Variable '" + variableName.toString + "' in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "' is declared multiple times!")
      case _ => typeEnvironment + (variableName.name -> variableType)
    }
  }

  override def allNames = variableType.allNames ++ variableName.allNames

  override def rename(renaming: Renaming) = LocalVariableDeclaration(variableType.rename(renaming), variableName.rename(renaming).asInstanceOf[VariableName])

  override def toString = variableType.toString + " " + variableName.toString + ";"
}
