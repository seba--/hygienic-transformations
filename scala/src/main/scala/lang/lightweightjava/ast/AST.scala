package lang.lightweightjava.ast

import name.namegraph.NameGraphExtended
import name.{Name, Nominal, Renaming}
import ref.Structural

object AST {
  def isLegalName(name: Name) = name.length > 0 && name.length < 256 &&
    Character.isJavaIdentifierStart(name.charAt(0)) &&
    name.forall(Character.isJavaIdentifierPart) && !isKeyword(name)

  def isKeyword(name: Name) = Set("this", "class", "public", "private", "extends", "return", "if", "else", "new", "null").contains(name)

  def genFreshName(usedNames: Set[Name], oldName: Name, counter : Int = 0) : Name = {
    val currentName = oldName + counter
    if (usedNames.contains(currentName)) genFreshName(usedNames, oldName, counter + 1)
    else currentName
  }
}

trait AST extends Nominal {
  override def rename(renaming: Renaming): AST

  def resolveNames(nameEnvironment : ClassNameEnvironment): NameGraphExtended

  override def resolveNames = resolveNames(Map())

  override def asStructural: Structural = ???
}
