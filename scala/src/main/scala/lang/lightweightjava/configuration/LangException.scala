package lang.lightweightjava.configuration

case class LangException(message: String) {
  override def toString = message
}

object NullPointerException extends LangException("NullPointerException")
