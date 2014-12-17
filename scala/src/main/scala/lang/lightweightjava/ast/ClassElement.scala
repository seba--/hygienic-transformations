package lang.lightweightjava.ast

import name.NameGraph

abstract class ClassElement extends AST {
  override def rename(renaming: RenamingFunction): ClassElement

  override def resolveNames(nameEnvironment: ClassNameEnvironment): NameGraph = resolveNames(nameEnvironment, null)

  def resolveNames(nameEnvironment: ClassNameEnvironment, classDefinition : ClassDefinition): NameGraph

}
