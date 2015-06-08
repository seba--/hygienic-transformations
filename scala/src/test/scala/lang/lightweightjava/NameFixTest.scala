package lang.lightweightjava

import lang.lightweightjava.configuration.NormalConfiguration
import lang.lightweightjava.trans.localdeclaration.LocalDeclarationTransformation
import name.Nominal
import name.namefix.NameFix
import org.scalatest.{FlatSpec, Matchers}

class NameFixTest extends FlatSpec with Matchers {
  val p1 =
    "class A {\n" +
      "  A method_ldt(A test) {\n" +
      "   return test;\n" +
      "  }\n" +
      "}\n" +
      "class B extends A {\n" +
      "   A method() {\n" +
      "     A test;\n" +
      "     return this.method_ldt(this);\n" +
      "   }\n" +
      "}\n" +
      "\n"
  val st1 = "x = new B();\n" +
    "y = x.method();"
  val p2 = "class B {\n" +
    "   B method_ldt(B test) {\n" +
    "     return test;\n" +
    "   }\n" +
    "   B method() {\n" +
    "     B test;\n" +
    "     return this.method_ldt(this);\n" +
    "   }\n" +
    "}\n" +
    "\n"
  "Name Fix" should "fix the LDT test program" in (Parser.parseAll(Parser.configuration, p1 + st1) match {
    case Parser.Success(p, _) =>
      val pNameGraph = p.program.asInstanceOf[Nominal].resolveNames
      val pTransformed = LocalDeclarationTransformation.transform(p.program)
      val pTransformedNameGraph = pTransformed.asInstanceOf[Nominal].resolveNames
      val pFixed = NameFix.nameFixExtended(pNameGraph, pTransformed)
      val pFixedNameGraph = pFixed.asInstanceOf[Nominal].resolveNames

      // If NameFix did not fix the program, type checking or interpretation will fail
      val result = Interpreter.interpret(NormalConfiguration(pFixed, p.state, p.heap, p.asInstanceOf[NormalConfiguration].programFlow:_*))
      result.state("x") should be (result.state("y"))

      info("Name graph stats for P1 before transformation: " + pNameGraph.V.size + " nodes, " + pNameGraph.E.values.flatten.size + " edges")
      info("Name graph stats for P1 after transformation: " + pTransformedNameGraph.V.size + " nodes, " + pTransformedNameGraph.E.values.flatten.size + " edges")
      info("Name graph stats for P1 after NameFix: " + pFixedNameGraph.V.size + " nodes, " + pFixedNameGraph.E.values.flatten.size + " edges")
      info("NameFix P1 result: " + pFixed.toString);
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  "Name Fix" should "fix the LDT test program with conflicts" in (Parser.parseAll(Parser.configuration, p2 + st1) match {
    case Parser.Success(p, _) =>
      val pNameGraph = p.program.asInstanceOf[Nominal].resolveNames
      val pTransformed = LocalDeclarationTransformation.transform(p.program)
      val pTransformedNameGraph = pTransformed.asInstanceOf[Nominal].resolveNames
      val pFixed = NameFix.nameFixExtended(pNameGraph, pTransformed)
      val pFixedNameGraph = pFixed.asInstanceOf[Nominal].resolveNames

      // If NameFix did not fix the program, type checking or interpretation will fail
      val result = Interpreter.interpret(NormalConfiguration(pFixed, p.state, p.heap, p.asInstanceOf[NormalConfiguration].programFlow:_*))
      result.state("x") should be (result.state("y"))

      info("Name graph stats for P2 before transformation: " + pNameGraph.V.size + " nodes, " + pNameGraph.E.values.flatten.size + " edges")
      info("Name graph stats for P2 after transformation: " + pTransformedNameGraph.V.size + " nodes, " + pTransformedNameGraph.E.values.flatten.size + " edges")
      info("Name graph stats for P2 after NameFix: " + pFixedNameGraph.V.size + " nodes, " + pFixedNameGraph.E.values.flatten.size + " edges")
      info("NameFix P2 result: " + pFixed.toString);
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
}