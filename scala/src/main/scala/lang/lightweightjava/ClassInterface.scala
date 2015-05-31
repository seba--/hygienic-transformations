package lang.lightweightjava

import lang.lightweightjava.ast.ClassName
import name.{Renaming, Identifier, NameInterface}

case class ClassInterface(className: ClassName, exportedFields: Set[Identifier], exportedMethods: Set[Identifier]) extends NameInterface {
  override val moduleID = className

  override def export: Set[Identifier] = exportedFields ++ exportedMethods

  override def rename(renaming: Renaming) = ClassInterface(className, exportedFields.map(renaming(_)), exportedMethods.map(renaming(_)))
}
