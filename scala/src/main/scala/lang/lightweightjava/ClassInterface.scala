package lang.lightweightjava

import lang.lightweightjava.ast.ClassName
import name.{Identifier, Meta}

case class ClassInterface(className: ClassName, exportedFields: Set[Identifier], exportedMethods: Set[Identifier]) extends Meta {
  override val moduleID = className

  override def export: Set[Identifier] = exportedFields ++ exportedMethods
}
