package lang.lightweightjava

import lang.lightweightjava.ast.ClassName
import name.{Identifier, Meta}

class ClassInterface(val className: ClassName, val exportedFields: Set[Identifier], val exportedMethods: Set[Identifier]) extends Meta {

  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[ClassInterface] && obj.asInstanceOf[ClassInterface].className == className

  override def hashCode(): Int = className.hashCode()

  override val moduleID = className

  override def export: Set[Identifier] = exportedFields ++ exportedMethods
}
