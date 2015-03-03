package lang.lightweightjava.modular

import lang.lightweightjava.ast.Program
import lang.lightweightjava.configuration.NormalConfiguration
import lang.lightweightjava.localdeclaration.LocalDeclarationTransformation
import lang.lightweightjava.{ClassInterface, Interpreter, Parser}
import name.namefix.NameFix
import org.scalatest.{FlatSpec, Matchers}

class NameFixModularTest extends FlatSpec with Matchers {
  val m1a =
    "class A {\n" +
      "  A method_ldt(A test) {\n" +
      "   return test;\n" +
      "  }\n" +
      "}\n"
  val m1b = "class B extends A {\n" +
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
  "Name Fix" should "fix the modular LDT test program with a cross-module capture" in ((Parser.parseAll(Parser.classDef, m1a), Parser.parseAll(Parser.classDef, m1b)) match {
    case (Parser.Success(originalA, _), Parser.Success(originalB, _)) =>
      val p = Parser.parseAll(Parser.configuration, st1).get

      val (originalGraphA, originalMetaA) = originalA.resolveNamesModular()
      val (originalGraphB, _) = originalB.resolveNamesModular(Set(originalMetaA))

      val transformedA = LocalDeclarationTransformation.transformClass(originalA)
      val transformedB = LocalDeclarationTransformation.transformClass(originalB)
      val (transformedGraphA, transformedMetaA) = transformedA.resolveNamesModular()
      val (transformedGraphB, _) = transformedB.resolveNamesModular(Set(transformedMetaA))

      val fixedModules = NameFix.nameFix(Set(originalA, originalB), Set[ClassInterface](), Set(transformedA, transformedB), Set[ClassInterface]())
      val (fixedGraphA, fixedMetaA) = fixedModules.find(_.moduleID.name == "A").get.resolveNamesModular()
      val (fixedGraphB, _) = fixedModules.find(_.moduleID.name == "B").get.resolveNamesModular(Set(fixedMetaA))

      // If NameFix did not fix the program, type checking or interpretation will fail
      val result = Interpreter.interpret(NormalConfiguration(Program(fixedModules.toSeq:_*), p.state, p.heap, p.asInstanceOf[NormalConfiguration].programFlow:_*))
      result.state("x") should be (result.state("y"))

      info("Name graph stats for M1A before transformation: " + originalGraphA.V.size + " nodes, " + originalGraphA.E.values.flatten.size + " internal edges, " + originalGraphA.EOut.values.flatten.size + " external edges")
      info("Name graph stats for M1A after transformation: " + transformedGraphA.V.size + " nodes, " + transformedGraphA.E.values.flatten.size + " internal edges, " + transformedGraphA.EOut.values.flatten.size + " external edges")
      info("Name graph stats for M1A after fixing: " + fixedGraphA.V.size + " nodes, " + fixedGraphA.E.values.flatten.size + " internal edges, " + fixedGraphA.EOut.values.flatten.size + " external edges")
      info("Name graph stats for M1B before transformation: " + originalGraphB.V.size + " nodes, " + originalGraphB.E.values.flatten.size + " internal edges, " + originalGraphB.EOut.values.flatten.size + " external edges")
      info("Name graph stats for M1B after transformation: " + transformedGraphB.V.size + " nodes, " + transformedGraphB.E.values.flatten.size + " internal edges, " + transformedGraphB.EOut.values.flatten.size + " external edges")
      info("Name graph stats for M1B after fixing: " + fixedGraphB.V.size + " nodes, " + fixedGraphB.E.values.flatten.size + " internal edges, " + fixedGraphB.EOut.values.flatten.size + " external edges")
    case _ => fail("Parsing error!")
  })
}