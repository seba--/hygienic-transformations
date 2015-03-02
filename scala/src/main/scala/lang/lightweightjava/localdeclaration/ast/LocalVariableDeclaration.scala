package lang.lightweightjava.localdeclaration.ast

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.statement.{Statement, This, VariableName}
import name.Renaming
import name.namegraph.NameGraph

case class LocalVariableDeclaration(variableType: ClassRef, name: VariableName) extends Statement {
  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment: TypeEnvironment) = {
    val redefinedVar =
      if (methodEnvironment.contains(name.name)) NameGraph(Set(), Map(name -> methodEnvironment(name.name)))
      else NameGraph(Set(), Map())

    (variableType.resolveNames(nameEnvironment) + name.resolveVariableNames(methodEnvironment + (name.name -> name)) + redefinedVar,
      (methodEnvironment + (name.name -> name), typeEnvironment + (name -> variableType)))
  }

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    require(variableType match {
      case className@ClassName(_) => program.getClassDefinition(className).isDefined
      case _ => true
    }, "Could not find definition of type '" + variableType.toString + "' for declaration of variable '" + name.toString + "' in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
    typeEnvironment.get(name) match {
      case Some(ClassName(_)) => sys.error("Variable '" + name.toString + "' in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "' is declared multiple times!")
      case _ => typeEnvironment + (name -> variableType)
    }
  }

  override def allNames = variableType.allNames ++ name.allNames

  override def rename(renaming: Renaming) = LocalVariableDeclaration(variableType.rename(renaming), name.rename(renaming).asInstanceOf[VariableName])

  override def toString = variableType.toString + " " + name.toString + ";"
}
