package ref

import name.Identifier.ID
import name.Name

trait Declaration {
  protected[ref] var id: ID = new ID()

  final override def equals(obj: scala.Any) = obj.isInstanceOf[Declaration] && obj.asInstanceOf[Declaration].id == id
  final override def hashCode(): Int = id.hashCode()
}
