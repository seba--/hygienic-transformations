package ref

import name.Identifier.ID

trait Reference {
  protected[ref] var id: ID = new ID()
  protected def withID(newid: ID): this.type = {id = newid; this}
  protected def withID(from: Reference): this.type = {id = from.id; this}

  final override def equals(obj: scala.Any) = obj.isInstanceOf[Reference] && obj.asInstanceOf[Reference].id == id
  final override def hashCode(): Int = id.hashCode()

  def target: Declaration
  def retarget(newtarget: Declaration): Reference
}
