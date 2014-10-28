package lang.lightweightjava.configuration

abstract class Value

case class OID(objectIdentifier: String) extends Value

object NullValue extends Value {
  override def toString: String = "null"
}
