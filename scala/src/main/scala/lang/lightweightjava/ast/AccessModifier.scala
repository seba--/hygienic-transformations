package lang.lightweightjava.ast

object AccessModifier extends Enumeration {
  type AccessModifier = Value
  val PUBLIC = Value("public")
  val PRIVATE = Value("private")
}
