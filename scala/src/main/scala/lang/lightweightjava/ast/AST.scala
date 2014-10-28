package lang.lightweightjava.ast

import name.{Name, NameGraph, Nominal}

object AST {
  def isLegalName(name: Name) = name.name.length > 0 && name.name.length < 256 &&
    Character.isJavaIdentifierStart(name.name.charAt(0)) &&
    name.name.forall(Character.isJavaIdentifierPart) && !isKeyword(name)

  def isKeyword(name: Name) = Set("this", "class", "public", "private", "extends", "return", "if", "else", "new", "null").contains(name.name)

  def genFreshName(usedNames: Set[String], oldName: Name, counter : Int = 0) : Name = {
    val currentName = oldName.name + counter
    if (usedNames.contains(currentName)) genFreshName(usedNames, oldName, counter + 1)
    else Name(currentName)
  }
}

abstract class AST extends Nominal {
  override def rename(renaming: Renaming): AST

  def resolveNames(nameEnvironment : ClassNameEnvironment): NameGraph

  override def resolveNames: NameGraph = resolveNames(Map())
}
