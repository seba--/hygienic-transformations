package lang.lightweightjava

import lang.lightweightjava.ast.statement.{Null, VariableName}
import org.scalatest.{FlatSpec, Matchers}

class InterpreterTest extends FlatSpec with Matchers {
  val p1 =
      "class X {\n" +
      "   X field;\n" +
      "   X m(X var1, Object var2) {\n" +
      "     if (var1 == var2)\n" +
      "       this.field = var1;\n" +
      "     else {\n" +
      "       this.field = null;\n" +
      "     }\n" +
      "     return var1;\n" +
      "   }\n" +
      "}\n"
  val p2 =
    "class Y extends X {\n" +
      "   X m(X var1, Object var2) {\n" +
      "     if (var1 == var2)\n" +
      "       this.field = null;\n" +
      "     else {\n" +
      "       this.field = var1;\n" +
      "     }\n" +
      "     return var1;\n" +
      "   }\n" +
      "}\n"
  val st1 = "x = new X();\n" +
      "y = new X();\n" +
      "z = x;\n" +
      "y.field = x;\n" +
      "x = x.m(y, y);\n" +
      "z.m(x, z);"
  val st2 = "x = new Y();\n" +
    "y = new X();\n" +
    "z = x;\n" +
    "y.field = x;\n" +
    "x = x.m(y, y);\n" +
    "z.m(x, z);"

  "Interpreter" should "correctly interpret the valid example program" in (Parser.parseAll(Parser.configuration, p1 + st1) match {
    case Parser.Success(p, _) =>
      val interpResult = Interpreter.interpret(p)
      // x == y
      interpResult.state(VariableName("x")) should be (interpResult.state(VariableName("y")))
      // x.field == z
      interpResult.heap(interpResult.state(VariableName("x")))._2("field") should be (interpResult.state(VariableName("z")))
      // y.field == z
      interpResult.heap(interpResult.state(VariableName("y")))._2("field") should be (interpResult.state(VariableName("z")))
      // z.field == null
      interpResult.heap(interpResult.state(VariableName("z")))._2("field") should be (interpResult.state(Null))
      info("P1 result:\n" + interpResult.toString)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })

  it should "correctly interpret the valid example program with inheritance/overriding" in (Parser.parseAll(Parser.configuration, p1 + p2 + st2) match {
    case Parser.Success(p, _) =>
      val interpResult = Interpreter.interpret(p)
      // x == y
      interpResult.state(VariableName("x")) should be (interpResult.state(VariableName("y")))
      // x.field == z
      interpResult.heap(interpResult.state(VariableName("x")))._2("field") should be (interpResult.state(VariableName("z")))
      // y.field == z
      interpResult.heap(interpResult.state(VariableName("y")))._2("field") should be (interpResult.state(VariableName("z")))
      // z.field == x
      interpResult.heap(interpResult.state(VariableName("z")))._2("field") should be (interpResult.state(VariableName("x")))
      info("\n\n\nP2 result:\n" + interpResult.toString)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
}
