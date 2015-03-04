package lang.lightweightjava.ast

import lang.lightweightjava.ast.AccessModifier._
import name.namegraph.NameGraphExtended
import name.{Identifier, Renaming}

case class FieldDeclaration(accessModifier: AccessModifier, fieldType: ClassRef, fieldName: Identifier) extends ClassElement {
  require(AST.isLegalName(fieldName.name), "Field name '" + fieldName + "' is no legal Java field name")

  override def allNames = fieldType.allNames + fieldName.name

  override def rename(renaming: Renaming) = FieldDeclaration(accessModifier, fieldType.rename(renaming), renaming(fieldName))

  override def resolveNames(nameEnvironment: ClassNameEnvironment, classDefinition: ClassDefinition) = {

    fieldType.resolveNames(nameEnvironment) + NameGraphExtended(Set(fieldName), Map())
  }

  override def toString = accessModifier.toString + " " + fieldType.toString + " " + fieldName.toString + ";"
}
