package lang.lightweightjava

import org.scalatest.{FlatSpec, Matchers}


class ParserTest extends FlatSpec with Matchers {
  val p1 =
      "class X {\n" +
      "   X field;\n" +
      "   Y field2;\n" +
      "   X m2(X var1, Y var2) {\n" +
      "     if (var1 == var2)\n" +
      "       var1 = new X();\n" +
      "     else {\n" +
      "       var2 = this.field;\n" +
      "     }\n" +
      "     return null;\n" +
      "   }\n" +
      "}\n"
  val st = "a = new X();\n" +
      "b = a.m2(a, null);"
  val p2 = "class Y extends X {\n" +
      "   Y m2(X var1, Y var2) {\n" +
      "     var1.field = var2;\n" +
      "     {" +
      "       var1 = var2.field;\n" +
      "       var2 = var1.m2(var1, this);\n" +
      "     }\n" +
      "     return var2;\n" +
      "   }\n" +
      "}\n"
  val p3 =
    "class X {" +
      "   X this;" +
      "   X m2(X var1, Y var2) {\n" +
      "     if (var1 == var2)\n" +
      "       var1 = new X();\n" +
      "     else {\n" +
      "       var2 = this.field;" +
      "     }" +
      "     return null;\n" +
      "   }\n" +
      "}\n"
  val p4 =
    "class X {" +
      "   X m2(X var1, Y var2) {\n" +
      "     if (var1 == var2)\n" +
      "       var1 = new X();\n" +
      "     else {\n" +
      "       this = this.field;" +
      "     }" +
      "     return null;\n" +
      "   }\n" +
      "}\n"
  "Parser" should "parse the valid example program" in (Parser.parseAll(Parser.program, p1 + p2) match {
    case Parser.Success(_, _) =>
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "parse the valid example configuration" in (Parser.parseAll(Parser.configuration, p1 + st) match {
    case Parser.Success(_, _) =>
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "not parse the valid example configuration as program" in (Parser.parseAll(Parser.program, p1 + st) match {
    case Parser.Failure(_, _) =>
    case Parser.Success(_, _) => fail("Parsing succeeded for an invalid program!")
    case Parser.Error(msg, _) => fail(msg)
  })
  it should "parse the valid example program without new lines" in (Parser.parseAll(Parser.program, (p1 + p2).replaceAll("\\s", " ")) match {
    case Parser.Success(_, _) =>
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "parse the semantically invalid example program" in (Parser.parseAll(Parser.program, p2) match {
    case Parser.Success(_, _) =>
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "not parse the first syntactically invalid example program" in (Parser.parseAll(Parser.program, p3) match {
    case Parser.Failure(_, _) =>
    case Parser.Success(_, _) => fail("Parsing succeeded for an invalid program!")
    case Parser.Error(msg, _) => fail(msg)
  })
  it should "not parse the second syntactically invalid example program" in (Parser.parseAll(Parser.program, p4) match {
    case Parser.Failure(_, _) =>
    case Parser.Success(_, _) => fail("Parsing succeeded for an invalid program!")
    case Parser.Error(msg, _) => fail(msg)
  })

  val p =
    "class Example {\n\tExample field;\n\t\n\tObject method(Example param) {\n\t\tparam.field = this;\n\t\tthis.field = param;\n\t\treturn param;\n\t}\n}"
  val r = Parser.parseAll(Parser.program, p)
  val r2 = r;
}
