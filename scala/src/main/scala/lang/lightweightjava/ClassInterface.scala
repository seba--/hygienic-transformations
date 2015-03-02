package lang.lightweightjava

import name.{Identifier, Meta}

class ClassInterface(val className: Identifier, val exportedFields: Set[Identifier], val exportedMethods: Set[Identifier]) extends Meta {

  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[ClassInterface] && obj.asInstanceOf[ClassInterface].className == className

  override def hashCode(): Int = className.hashCode()

  override val moduleID = className

  override def export: Set[Identifier] = exportedFields ++ exportedMethods
}
