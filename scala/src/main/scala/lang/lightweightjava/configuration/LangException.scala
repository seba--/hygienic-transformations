package lang.lightweightjava.configuration

case class LangException(message: String) {
  override def toString: String = message
}

object NullPointerException extends LangException("NullPointerException")
