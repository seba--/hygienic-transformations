package lang.lightweightjava

import lang.lightweightjava.configuration.{NormalConfiguration, NullValue}
import lang.lightweightjava.trans.whileloops.WhileLoopTransformation
import org.scalatest.{FlatSpec, Matchers}

class WhileLoopTest extends FlatSpec with Matchers {
  val p1 = "class Class {\n" +
      "    Class method(Class a, Class b) {\n" +
      "     if (a == b)" +
      "       a = null;\n" +
      "     else\n" +
      "       while (a != b)\n" +
      "         a = b;\n" +
      "     return a;" +
      "   }\n" +
      "}\n" +
      "\n"
  val st1 = "x = new Class();\n" +
    "y = new Class();\n" +
    "y = x.method(x, y);\n" +
    "z = x.method(x, x);"


  "WhileLoopTransformation" should "transform the While loop program to a valid, interpretable AST" in (Parser.parseAll(Parser.configuration, p1 + st1) match {
    case Parser.Success(p, _) =>
      val transformedProgram = WhileLoopTransformation.transform(p.program)
      val interpResult = Interpreter.interpret(NormalConfiguration(transformedProgram, p.state, p.heap, p.asInstanceOf[NormalConfiguration].programFlow:_*))
      // x != y
      interpResult.state.find(_._1 == "x").get._2 shouldNot be (interpResult.state("y"))
      // z == null
      interpResult.state.find(_._1 == "z").get._2 should be (NullValue)

      info("LDT result:\n" + interpResult.toString)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
}
