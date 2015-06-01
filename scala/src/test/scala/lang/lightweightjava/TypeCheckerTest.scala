package lang.lightweightjava

import org.scalatest.{FlatSpec, Matchers}

class
TypeCheckerTest extends FlatSpec with Matchers {
  val p1 =
      "class X {\n" +
      "   X field;\n" +
      "   X m2(X var1, X var2) {\n" +
      "     if (var1 == var2)\n" +
      "       var1 = new Y();\n" +
      "     else {\n" +
      "       var2 = this.field;\n" +
      "     }\n" +
      "     return new Y();\n" +
      "   }\n" +
      "}\n"
  val p2 = "class Y extends X {\n" +
      "   X m2(X var1, X var2) {\n" +
      "     var1.field = var2;\n" +
      "     {" +
      "       var1 = var2.field;\n" +
      "       var2 = var1.m2(var1, this);\n" +
      "     }\n" +
      "     return var2;\n" +
      "   }\n" +
      "}\n"
  val pCycle =
    "class X extends Y {\n" +
      "   X field;\n" +
      "   X m2(X var1, X var2) {\n" +
      "     if (var1 == var2)\n" +
      "       var1 = new X();\n" +
      "     else {\n" +
      "       var2 = this.field;\n" +
      "     }\n" +
      "     return null;\n" +
      "   }\n" +
      "}\n"
  val pOverwriteFail = "class Y extends X {\n" +
    "   X m2(X var1, Y var2) {\n" +
    "     var1.field = var2;\n" +
    "     {" +
    "       var1 = var2.field;\n" +
    "       var2 = var1.m2(var1, this);\n" +
    "     }\n" +
    "     return var2;\n" +
    "   }\n" +
    "}\n"
  val pOverwriteField = "class Y extends X {\n" +
    "   X field;\n" +
    "   X m2(X var1, X var2) {\n" +
    "     var1.field = var2;\n" +
    "     {" +
    "       var1 = var2.field;\n" +
    "       var2 = var1.m2(var1, this);\n" +
    "     }\n" +
    "     return var2;\n" +
    "   }\n" +
    "}\n"
  val pInvalidReturnType = "class Y extends X {\n" +
    "   X m2(X var1, X var2) {\n" +
    "     var1.field = var2;\n" +
    "     {" +
    "       var1 = var2.field;\n" +
    "       var2 = var1.m2(var1, this);\n" +
    "     }\n" +
    "     return new Object();\n" +
    "   }\n" +
    "}\n"
  val pInvalidAssignType = "class Y extends X {\n" +
    "   X m2(X var1, X var2) {\n" +
    "     var1.field = var2;\n" +
    "     {" +
    "       var1 = new Object();\n" +
    "       var2 = var1.m2(var1, this);\n" +
    "     }\n" +
    "     return var2;\n" +
    "   }\n" +
    "}\n"
  val pUnknownMethod = "class Y extends X {\n" +
    "   X m2(X var1, X var2) {\n" +
    "     var1.field = var2;\n" +
    "     {" +
    "       var1 = new Y();\n" +
    "       var2 = var1.m3(var1, this);\n" +
    "     }\n" +
    "     return var2;\n" +
    "   }\n" +
    "}\n"
  val pInvalidComparison =
    "class X {\n" +
    "}\n" +
    "class Z {\n" +
    "   Z m2(X var1, Z var2) {\n" +
    "     if (var1 == var2)\n" +
    "       var1 = new X();\n" +
    "     else {\n" +
    "       var2 = new Z();\n" +
    "     }\n" +
    "     return null;\n" +
    "   }\n" +
    "}\n"
  "Type checker" should "succeed for the valid example program" in (Parser.parseAll(Parser.program, p1 + p2) match {
    case Parser.Success(p, _) => p.typeCheck
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "fail for a class with an unknown super-class" in (Parser.parseAll(Parser.program, p2) match {
    case Parser.Success(p, _) => info("Type checker error: " + intercept[IllegalArgumentException] {
      p.typeCheck
    }.getMessage)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "fail for a cyclic inheritance path" in (Parser.parseAll(Parser.program, pCycle + p2) match {
    case Parser.Success(p, _) => info("Type checker error: " + intercept[IllegalArgumentException] {
      p.typeCheck
    }.getMessage)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "fail for overwriting of a method with different parameter types" in (Parser.parseAll(Parser.program, p1 + pOverwriteFail) match {
    case Parser.Success(p, _) => info("Type checker error: " + intercept[IllegalArgumentException] {
      p.typeCheck
    }.getMessage)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "fail for overshadowing of a field" in (Parser.parseAll(Parser.program, p1 + pOverwriteField) match {
    case Parser.Success(p, _) => info("Type checker error: " + intercept[IllegalArgumentException] {
      p.typeCheck
    }.getMessage)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "fail for an invalid return type" in (Parser.parseAll(Parser.program, p1 + pInvalidReturnType) match {
    case Parser.Success(p, _) => info("Type checker error: " + intercept[IllegalArgumentException] {
      p.typeCheck
    }.getMessage)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "fail for invalid assignment types" in (Parser.parseAll(Parser.program, p1 + pInvalidAssignType) match {
    case Parser.Success(p, _) => info("Type checker error: " + intercept[IllegalArgumentException] {
      p.typeCheck
    }.getMessage)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "fail for a call of an unknown method" in (Parser.parseAll(Parser.program, p1 + pUnknownMethod) match {
    case Parser.Success(p, _) => info("Type checker error: " + intercept[IllegalArgumentException] {
      p.typeCheck
    }.getMessage)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "fail for a comparison between two unrelated types" in (Parser.parseAll(Parser.program, pInvalidComparison) match {
    case Parser.Success(p, _) => info("Type checker error: " + intercept[IllegalArgumentException] {
      p.typeCheck
    }.getMessage)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
}
