package lang.lightweightjava

import lang.lightweightjava.ast.ClassName
import name.{Identifier, MetaInterface}

case class ClassInterface(className: ClassName, exportedFields: Set[Identifier], exportedMethods: Set[Identifier]) extends MetaInterface {
  override val moduleID = className

  override def export: Set[Identifier] = exportedFields ++ exportedMethods
}
