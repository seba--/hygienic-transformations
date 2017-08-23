package ref

import name.Identifier.ID

trait Reference {
  protected[ref] var _id: ID = new ID()
  def id = _id
  def withID(newid: ID): this.type = {_id = newid; this}
  def withID(from: Reference): this.type = {_id = from._id; this}

  final override def equals(obj: scala.Any) = obj.isInstanceOf[Reference] && obj.asInstanceOf[Reference].id == id
  final override def hashCode(): Int = id.hashCode()

  def target: Declaration
  def hasTarget: Boolean
  def retarget(newtarget: Option[Declaration]): Reference
}
