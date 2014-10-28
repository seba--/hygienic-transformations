package lang.lightweightjava

import lang.lightweightjava.ast.statement.VariableName
import lang.lightweightjava.configuration.NormalConfiguration
import lang.lightweightjava.localdeclaration.LocalDeclarationTransformation
import org.scalatest.{FlatSpec, Matchers}

class LocalDeclarationTest extends FlatSpec with Matchers {
  val p1 = "class B {\n" +
      "   B field;\n" +
      "   B method() {\n" +
      "     B test;\n" +
      "     test = new B();\n" +
      "     test.field = test;\n" +
      "     return test.method2(test);\n" +
      "   }\n" +
      "   B method2(B var) {\n" +
      "     return var.field;\n" +
      "   }\n" +
      "}\n"
  val st1 = "x = new B();\n" +
      "y = x.method();"

  "LocalDeclarationTransformation" should "transform the LDT program to a valid, interpretable AST" in (Parser.parseAll(Parser.configuration, p1 + st1) match {
    case Parser.Success(p, _) =>
      val transformedProgram = LocalDeclarationTransformation.transform(p.program)
      val interpResult = Interpreter.interpret(NormalConfiguration(transformedProgram, p.state, p.heap, p.asInstanceOf[NormalConfiguration].programFlow:_*))
      // y.field == y
      interpResult.heap(interpResult.state(VariableName("y")))._2("field") should be (interpResult.state(VariableName("y")))
      info("LDT result:\n" + interpResult.toString)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
}
