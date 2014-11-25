package lang.lightweightjava.ast

import lang.lightweightjava.ast.AccessModifier._
import name.{Name, NameGraph}

case class FieldDeclaration(accessModifier: AccessModifier, fieldType: ClassRef, fieldName: Name) extends ClassElement {
  require(AST.isLegalName(fieldName), "Field name '" + fieldName + "' is no legal Java field name")

  override def allNames = fieldType.allNames + fieldName.id

  override def rename(renaming: Renaming) = FieldDeclaration(accessModifier, fieldType.rename(renaming), renaming(fieldName))

  override def resolveNames(nameEnvironment: ClassNameEnvironment, classDefinition: ClassDefinition): NameGraph = {
    val fieldNameGraph = accessModifier match {
      case PUBLIC => NameGraph(Set((fieldName.id, true)), Map(), Set())
      case PRIVATE => NameGraph(Set((fieldName.id, false)), Map(), Set())
    }
    fieldType.resolveNames(nameEnvironment) ++ fieldNameGraph
  }

  override def toString: String = accessModifier.toString + " " + fieldType.toString + " " + fieldName.toString + ";"
}
