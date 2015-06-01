package lang.lightweightjava.ast

import name.Renaming
import name.namegraph.NameGraphExtended

abstract class ClassElement extends AST {
  override def rename(renaming: Renaming): ClassElement

  override def resolveNames(nameEnvironment: ClassNameEnvironment) = resolveNames(nameEnvironment, null)

  def resolveNames(nameEnvironment: ClassNameEnvironment, classDefinition : ClassDefinition): NameGraphExtended

}
