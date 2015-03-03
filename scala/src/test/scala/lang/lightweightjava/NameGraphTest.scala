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
  val p2 =
    "class Y extends X {\n" +
      "   X m2(X var1, X var2) {\n" +
      "     if (var1 == var2)\n" +
      "       var1 = new X();\n" +
      "     else {\n" +
      "       var2 = var1.field;\n" +
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
  it should "contain 16 nodes and 11 edges from 11 nodes for the correct program with method overriding" in (Parser.parseAll(Parser.program, p1 + p2) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.V.size should be (31)
      nameGraph.E.size should be (25)
      nameGraph.E.values.flatten.size should be (25)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain 16 nodes and 11 edges from 11 nodes for the program with an undefined class reference" in (Parser.parseAll(Parser.program, p2) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.V.size should be (15)
      nameGraph.E.size should be (6)
      nameGraph.E.values.flatten.size should be (6)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain 18 nodes and 16 edges from 14 nodes for the program with an undefined variable reference" in (Parser.parseAll(Parser.program, pUndefinedVariable) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.V.size should be (16)
      nameGraph.E.size should be (10)
      nameGraph.E.values.flatten.size should be (10)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain 18 nodes and 16 edges from 14 nodes for the program with overlapping method definitions" in (Parser.parseAll(Parser.program, pOverlappingMethods) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.V.size should be (18)
      nameGraph.E.size should be (14)
      nameGraph.E.values.flatten.size should be (14)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain 0 nodes and 0 edges from 0 nodes for the program with multiple declarations of the same field" in (Parser.parseAll(Parser.program, pMultipleFields) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.V.size should be (17)
      nameGraph.E.size should be (13)
      nameGraph.E.values.flatten.size should be (14)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain 18 nodes and 16 edges from 14 nodes for the program with overlapping method definitions and inheritance" in (Parser.parseAll(Parser.program, pOverlappingMethods + p2) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.V.size should be (33)
      nameGraph.E.size should be (27)
      nameGraph.E.values.flatten.size should be (30)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain 0 nodes and 0 edges from 0 nodes for the program with multiple declarations of the same field and inheritance" in (Parser.parseAll(Parser.program, pMultipleFields + p2) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.V.size should be (32)
      nameGraph.E.size should be (27)
      nameGraph.E.values.flatten.size should be (29)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
}