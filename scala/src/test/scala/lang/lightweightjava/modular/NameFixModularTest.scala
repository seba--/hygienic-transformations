package lang.lightweightjava.modular

import lang.lightweightjava.ast.{ClassName, Program}
import lang.lightweightjava.configuration.NormalConfiguration
import lang.lightweightjava.trans.localdeclaration.LocalDeclarationTransformation
import lang.lightweightjava.{ClassInterface, Interpreter, Parser}
import name.Identifier
import name.namefix.NameFix
import name.namegraph.NameGraphModular
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

  val m2a =
    "class A {\n" +
      "  A method(A test) {\n" +
      "   Object x;\n" +
      "   return test;\n" +
      "  }\n" +
      "  A method_ldt(A test) {\n" +
      "   return test;\n" +
      "  }\n" +
      "}\n"
  val m2b = "class B extends A {\n" +
    "   A method(A test) {\n" +
    "     return this.method_ldt(this);\n" +
    "   }\n" +
    "}\n" +
    "\n"

  val m3b = "class B extends A {\n" +
    "   A method() {\n" +
    "     A test;\n" +
    "     return this.method_ldt(this);\n" +
    "   }\n" +
    "}\n" +
    "\n"

  val st = "x = new B();\n" +
    "y = x.method();"

  "Modular Name Fix" should "fix the modular LDT test program with a cross-module capture" in ((Parser.parseAll(Parser.classDef, m1a), Parser.parseAll(Parser.classDef, m1b)) match {
    case (Parser.Success(originalA, _), Parser.Success(originalB, _)) =>
      val p = Parser.parseAll(Parser.configuration, st).get

      val originalGraphA = originalA.resolveNamesModular
      val originalGraphB = originalB.link(originalGraphA.I).resolveNamesModular

      val transformedA = LocalDeclarationTransformation.transformClass(originalA)
      val transformedGraphA = transformedA.resolveNamesModular
      val fixedProgA = NameFix.nameFixModular(originalGraphA, transformedA, Set())
      val fixedGraphA = fixedProgA.resolveNamesModular

      val transformedB = LocalDeclarationTransformation.transformClass(originalB)
      val transformedGraphB = transformedB.link(fixedGraphA.I).resolveNamesModular
      val fixedProgB = NameFix.nameFixModular(originalGraphB, transformedB, Set(fixedProgA.interface))
      val fixedGraphB = fixedProgB.resolveNamesModular

      info("Name graph stats for M1A before transformation: " + originalGraphA.V.size + " nodes, " + intEdges(originalGraphA) + " internal edges, " + extEdges(originalGraphA) + " external edges")
      info("Name graph stats for M1A after transformation: " + transformedGraphA.V.size + " nodes, " + intEdges(transformedGraphA) + " internal edges, " + extEdges(transformedGraphA) + " external edges")
      info("Name graph stats for M1A after fixing: " + fixedGraphA.V.size + " nodes, " + intEdges(fixedGraphA) + " internal edges, " + extEdges(fixedGraphA) + " external edges")
      info("Name graph stats for M1B before transformation: " + originalGraphB.V.size + " nodes, " + intEdges(originalGraphB) + " internal edges, " + extEdges(originalGraphB) + " external edges")
      info("Name graph stats for M1B after transformation: " + transformedGraphB.V.size + " nodes, " + intEdges(transformedGraphB) + " internal edges, " + extEdges(transformedGraphB) + " external edges")
      info("Name graph stats for M1B after fixing: " + fixedGraphB.V.size + " nodes, " + intEdges(fixedGraphB) + " internal edges, " + extEdges(fixedGraphB) + " external edges")

      // If NameFix did not fix the program, type checking or interpretation will fail
      val result = Interpreter.interpret(NormalConfiguration(Program(fixedProgA, fixedProgB), p.state, p.heap, p.asInstanceOf[NormalConfiguration].programFlow:_*))
      result.state("x") should be (result.state("y"))

    case _ => fail("Parsing error!")
  })
  it should "propagate and fix the modular LDT test program with a cross-module capture" in ((Parser.parseAll(Parser.classDef, m2a), Parser.parseAll(Parser.classDef, m2b)) match {
    case (Parser.Success(originalA, _), Parser.Success(originalB, _)) =>
      val p = Parser.parseAll(Parser.configuration, st).get

      val originalGraphA = originalA.resolveNamesModular
      val originalGraphB = originalB.link(originalA.interface).resolveNamesModular

      val transformedA = LocalDeclarationTransformation.transformClass(originalA)
      val transformedGraphA = transformedA.resolveNamesModular
      val fixedProgA = NameFix.nameFixModular(originalGraphA, transformedA, Set())
      val fixedGraphA = fixedProgA.resolveNamesModular

      val transformedB = LocalDeclarationTransformation.transformClass(originalB)
      val transformedGraphB = transformedB.link(fixedGraphA.I).resolveNamesModular
      val fixedProgB = NameFix.nameFixModular(originalGraphB, transformedB, Set(fixedProgA.interface))
      val fixedGraphB = fixedProgB.resolveNamesModular

      // If NameFix did not fix the program, type checking or interpretation will fail
      val result = Interpreter.interpret(NormalConfiguration(Program(fixedProgA, fixedProgB), p.state, p.heap, p.asInstanceOf[NormalConfiguration].programFlow:_*))
      result.state("x") should be (result.state("y"))

      info("Name graph stats for M2A before transformation: " + originalGraphA.V.size + " nodes, " + intEdges(originalGraphA) + " internal edges, " + extEdges(originalGraphA) + " external edges")
      info("Name graph stats for M2A after transformation: " + transformedGraphA.V.size + " nodes, " + intEdges(transformedGraphA) + " internal edges, " + extEdges(transformedGraphA) + " external edges")
      info("Name graph stats for M2A after fixing: " + fixedGraphA.V.size + " nodes, " + intEdges(fixedGraphA) + " internal edges, " + extEdges(fixedGraphA) + " external edges")
      info("Name graph stats for M2B before transformation: " + originalGraphB.V.size + " nodes, " + intEdges(originalGraphB) + " internal edges, " + extEdges(originalGraphB) + " external edges")
      info("Name graph stats for M2B after transformation: " + transformedGraphB.V.size + " nodes, " + intEdges(transformedGraphB) + " internal edges, " + extEdges(transformedGraphB) + " external edges")
      info("Name graph stats for M2B after fixing: " + fixedGraphB.V.size + " nodes, " + intEdges(fixedGraphB) + " internal edges, " + extEdges(fixedGraphB) + " external edges")
    case _ => fail("Parsing error!")
  })
  
  // Given program A for the following scenarios:
  //  class A {
  //    Object method(A test) {
  //      Object x;
  //      return test;
  //    }
  //    A method_ldt(A test) {
  //      return test;
  //    }
  //  }
  //
  // To solve the post-transformation conflict, NameFix can either rename the original or the synthesized method,
  // which leads to different situations for the dependent module B
  it should "fix the scenario (see comment in code) if the original method was renamed" in (Parser.parseAll(Parser.classDef, m3b) match {
    case Parser.Success(originalB, _) =>
      val classID = ClassName("A")
      val methodID = Identifier("method")
      val methodLdtSynID = Identifier("method_ldt")
      val methodLdtOrigID = Identifier("method_ldt")

      val metaOriginalA = ClassInterface(classID, Set(), Set(methodID, methodLdtOrigID))
      val metaTransformedA = ClassInterface(classID, Set(), Set(methodID, methodLdtSynID, methodLdtOrigID))
      val metaRenamedA = metaTransformedA.rename(Map(methodLdtOrigID -> "method_ldt_0")).asInstanceOf[ClassInterface]

      val originalGraphB = originalB.link(metaOriginalA).resolveNamesModular
      val transformedB = LocalDeclarationTransformation.transformClass(originalB, useAccessModifiers = false)
      transformedB.link(metaRenamedA)
      NameFix.nameFixModular(originalGraphB, transformedB, Set(metaRenamedA))
    case _ => fail("Parsing error!")
  })
  it should "fix the scenario (see comment in code) if the synthesized method was renamed" in (Parser.parseAll(Parser.classDef, m3b) match {
    case Parser.Success(originalB, _) =>
      val classID = ClassName("A")
      val methodID = Identifier("method")
      val methodLdtSynID = Identifier("method_ldt")
      val methodLdtOrigID = Identifier("method_ldt")

      val metaOriginalA = ClassInterface(classID, Set(), Set(methodID, methodLdtOrigID))
      val metaTransformedA = ClassInterface(classID, Set(), Set(methodID, methodLdtOrigID, methodLdtSynID))
      val metaRenamedA = metaTransformedA.rename(Map(methodLdtSynID -> "method_ldt_0")).asInstanceOf[ClassInterface]

      val transformedB = LocalDeclarationTransformation.transformClass(originalB, useAccessModifiers = false)
      val originalGraphB = originalB.link(metaOriginalA).resolveNamesModular
      transformedB.link(metaRenamedA)
      NameFix.nameFixModular(originalGraphB, transformedB, Set(metaRenamedA))
    case _ => fail("Parsing error!")
  })

  protected def intEdges(g: NameGraphModular[ClassInterface]): Set[Identifier] = {
    g.E.values.flatten.toSet.intersect(g.V)
  }

  protected def extEdges(g: NameGraphModular[ClassInterface]): Set[Identifier] = {
    g.E.values.flatten.toSet.diff(g.V)
  }
}