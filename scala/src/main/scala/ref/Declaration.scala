package ref

import name.Identifier.ID

trait Declaration {
  protected[ref] var id: ID = new ID()
  def withID(newid: ID): this.type = {id = newid; this}
  def withID(from: Declaration): this.type = {id = from.id; this}

  final override def equals(obj: scala.Any) = obj.isInstanceOf[Declaration] && obj.asInstanceOf[Declaration].id == id
  final override def hashCode(): Int = id.hashCode()
}
