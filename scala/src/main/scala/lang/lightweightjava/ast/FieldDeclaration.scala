package lang.lightweightjava.ast

import lang.lightweightjava.ast.AccessModifier._
import name.{Name, NameGraph}

case class FieldDeclaration(accessModifier: AccessModifier, fieldType: ClassRef, fieldName: Name) extends ClassElement {
  require(AST.isLegalName(fieldName), "Field name '" + fieldName + "' is no legal Java field name")

  override def allNames = fieldType.allNames + fieldName.id

  override def rename(renaming: RenamingFunction) = FieldDeclaration(accessModifier, fieldType.rename(renaming), renaming(fieldName))

  override def resolveNames(nameEnvironment: ClassNameEnvironment, classDefinition: ClassDefinition): NameGraph = {
    val fieldNameGraph = NameGraph(Set(fieldName.id), Map(), Set())
    fieldType.resolveNames(nameEnvironment) ++ fieldNameGraph
  }

  override def toString: String = accessModifier.toString + " " + fieldType.toString + " " + fieldName.toString + ";"
}
