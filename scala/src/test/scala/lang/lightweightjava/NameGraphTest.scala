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

  "Name Graph" should "contain no errors for the correct program" in (Parser.parseAll(Parser.program, p1) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.C.size should be (0)
      info("Name graph stats for P1: " + nameGraph.V.size + " nodes, " + nameGraph.E.size + " edges, " + nameGraph.C.size + " errors")
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain one declaration conflict for the program with overlapping method definitions" in (Parser.parseAll(Parser.program, pOverlappingMethods) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.C.size should be (1)
      nameGraph.C.head.size should be (2)
      info("Name graph stats for POverlappingMethods: " + nameGraph.V.size + " nodes, " + nameGraph.E.size + " edges, " + nameGraph.C.size + " errors")
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain one declaration conflict for the program with multiple declarations of the same field" in (Parser.parseAll(Parser.program, pMultipleFields) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.C.size should be (1)
      nameGraph.C.head.size should be (2)
      info("Name graph stats for PMultipleFields: " + nameGraph.V.size + " nodes, " + nameGraph.E.size + " edges, " + nameGraph.C.size + " errors")
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
}
