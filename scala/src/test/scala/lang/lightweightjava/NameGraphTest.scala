package lang.lightweightjava

import name.Nominal
import org.scalatest.{FlatSpec, Matchers}

class NameGraphTest extends FlatSpec with Matchers {
  val p1 =
    "class X {\n" +
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
  val pUndefinedClass =
    "class X {\n" +
      "   Y field;\n" +
      "   X m2(X var1, X var2) {\n" +
      "     if (var1 == var2)\n" +
      "       var1 = new X();\n" +
      "     else {\n" +
      "       var2 = this.field;\n" +
      "     }\n" +
      "     return null;\n" +
      "   }\n" +
      "}\n"
  val pUndefinedVariable =
    "class X {\n" +
      "   X field;\n" +
      "   X m2(X var1, X var2) {\n" +
      "     if (var1 == var3)\n" +
      "       var1 = new X();\n" +
      "     else {\n" +
      "       var2 = this.field;\n" +
      "     }\n" +
      "     return null;\n" +
      "   }\n" +
      "}\n"
  val pOverlappingMethods =
    "class X {\n" +
      "   X field;\n" +
      "   X m2(X var1, X var2) {\n" +
      "     if (var1 == var2)\n" +
      "       var1 = new X();\n" +
      "     else {\n" +
      "       var2 = this.field;\n" +
      "     }\n" +
      "     return null;\n" +
      "   }\n" +
      "   X m2() {\n" +
      "     return null;\n" +
      "   }\n" +
      "}\n"
  val pMultipleFields =
    "class X {\n" +
      "   X field;\n" +
      "   Object field;\n" +
      "   X m2(X var1, X var2) {\n" +
      "     if (var1 == var2)\n" +
      "       var1 = new X();\n" +
      "     else {\n" +
      "       var2 = this.field;\n" +
      "     }\n" +
      "     return null;\n" +
      "   }\n" +
      "}\n"

  "Name Graph" should "contain 16 nodes and 11 edges from 11 nodes for the correct program" in (Parser.parseAll(Parser.program, p1) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.V.size should be (16)
      nameGraph.E.size should be (11)
      nameGraph.E.values.flatten.size should be (11)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain 18 nodes and 16 edges from 14 nodes for the program with overlapping method definitions" in (Parser.parseAll(Parser.program, pOverlappingMethods) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.V.size should be (18)
      nameGraph.E.size should be (14)
      nameGraph.E.values.flatten.size should be (16)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain 0 nodes and 0 edges from 0 nodes for the program with multiple declarations of the same field" in (Parser.parseAll(Parser.program, pMultipleFields) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.V.size should be (17)
      nameGraph.E.size should be (13)
      nameGraph.E.values.flatten.size should be (16)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
}