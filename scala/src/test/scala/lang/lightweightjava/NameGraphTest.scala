package lang.lightweightjava

import name.NameGraph.{MultipleDeclarationsError, UnboundReferenceError}
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
      nameGraph.Err.size should be (0)
      info("Name graph stats for P1: " + nameGraph.V.size + " nodes, " + nameGraph.E.size + " edges, " + nameGraph.Err.size + " errors")
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain one unbound reference error for the program with one unbound class reference" in (Parser.parseAll(Parser.program, pUndefinedClass) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.Err.size should be (1)
      nameGraph.Err.head should matchPattern { case UnboundReferenceError(_) => }
      info("Name graph stats for PUndefinedClass: " + nameGraph.V.size + " nodes, " + nameGraph.E.size + " edges, " + nameGraph.Err.size + " errors")
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain one unbound reference error for the program with one unbound variable reference" in (Parser.parseAll(Parser.program, pUndefinedVariable) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.Err.size should be (1)
      nameGraph.Err.head should matchPattern { case UnboundReferenceError(_) => }
      info("Name graph stats for PUndefinedVariable: " + nameGraph.V.size + " nodes, " + nameGraph.E.size + " edges, " + nameGraph.Err.size + " errors")
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain one multiple declaration error for the program with overlapping method definitions" in (Parser.parseAll(Parser.program, pOverlappingMethods) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.Err.size should be (1)
      nameGraph.Err.head should matchPattern { case MultipleDeclarationsError(_) => }
      info("Name graph stats for POverlappingMethods: " + nameGraph.V.size + " nodes, " + nameGraph.E.size + " edges, " + nameGraph.Err.size + " errors")
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain one multiple declaration error for the program with multiple declarations of the same field" in (Parser.parseAll(Parser.program, pMultipleFields) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.asInstanceOf[Nominal].resolveNames
      nameGraph.Err.size should be (1)
      nameGraph.Err.head should matchPattern { case MultipleDeclarationsError(_) => }
      info("Name graph stats for PMultipleFields: " + nameGraph.V.size + " nodes, " + nameGraph.E.size + " edges, " + nameGraph.Err.size + " errors")
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
}
