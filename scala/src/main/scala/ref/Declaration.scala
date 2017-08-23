package ref

import name.Identifier.ID

trait Declaration {
  protected[ref] var _id: ID = new ID()
  def id = _id
  def withID(newid: ID): this.type = {_id = newid; this}
  def withID(from: Declaration): this.type = {_id = from._id; this}

  final override def equals(obj: scala.Any) = obj.isInstanceOf[Declaration] && obj.asInstanceOf[Declaration].id == id
  final override def hashCode(): Int = id.hashCode()
}

object Mock {
  def apply[T](use: Declaration => T): T = use(new Mock())
}
class Mock extends Declaration