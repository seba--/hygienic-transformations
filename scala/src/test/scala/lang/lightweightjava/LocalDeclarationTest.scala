package lang.lightweightjava

import lang.lightweightjava.ast.AccessModifier
import lang.lightweightjava.configuration.NormalConfiguration
import lang.lightweightjava.trans.localdeclaration.LocalDeclarationTransformation
import name.namefix.NameFix
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

  // Dummy classes for LJ integers and strings
  val intClass =
    "class int {" +
      "  public int negate() {" +
      "    return new int();" +
      "  }" +
      "  public int substract(int other) {" +
      "    return new int();" +
      "  }" +
      "}"
  val stringClass = "class String {\n" +
    "  public int size() {\n" +
    "    return new int();" +
    "  }\n" +
    "}\n"

  val myObject = "class MyObject {\n" +
    "  public String toString() {\n" +
    "    return new String();" +
    "  }\n" +
    "}\n"
  val base = "class Base {\n" +
    "  public int method(MyObject a) {\n" +
    "    String b;\n" +
    "    b = a.toString();\n" +
    "    b = b.concat(b);\n" +
    "    return this.methodHelper(b);\n" +
    "  }\n" +
    "  public int methodHelper(String s) {\n" +
    "    return s.size();\n" +
    "  }\n" +
    "}\n"

  "LocalDeclarationTransformation" should "transform the LDT program to a valid, interpretable AST" in (Parser.parseAll(Parser.configuration, p1 + st1) match {
    case Parser.Success(p, _) =>
      val transformedProgram = LocalDeclarationTransformation.transform(p.program)
      val interpResult = Interpreter.interpret(NormalConfiguration(transformedProgram, p.state, p.heap, p.asInstanceOf[NormalConfiguration].programFlow:_*))
      // y.field == y
      val yID = interpResult.state.find(_._1 == "y").get._2
      interpResult.heap(yID)._2("field") should be (yID)
      info("LDT result:\n" + interpResult.toString)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })

  "NameFix" should "fix the capture in class Base" in (Parser.parseAll(Parser.program, intClass + stringClass + myObject + base) match {
    case Parser.Success(p, _) =>
      val classInt = p.findClassDefinition("int").get
      val classString = p.findClassDefinition("String").get
      val classMyObject = p.findClassDefinition("MyObject").get
      val classBase = p.findClassDefinition("Base").get

      val baseDependencies = Set(classInt.interface, classString.interface, classMyObject.interface)

      classBase.link(baseDependencies)
      val classBaseTransformed = LocalDeclarationTransformation.transformClass(classBase)
      classBaseTransformed.link(baseDependencies)
      val transformedBaseGraph = classBaseTransformed.resolveNamesModular

      val methodHelperOrig = classBaseTransformed.methods.find(m => m.signature.methodName.name == "methodHelper" && m.signature.accessModifier == AccessModifier.PUBLIC).get
      val methodHelperSynth = classBaseTransformed.methods.find(m => m.signature.methodName.name == "methodHelper" && m.signature.accessModifier == AccessModifier.PRIVATE).get

      // Method definitions reference each other due to their conflict
      assert(transformedBaseGraph.E(methodHelperOrig.signature.methodName).contains(methodHelperSynth.signature.methodName))
      assert(transformedBaseGraph.E(methodHelperSynth.signature.methodName).contains(methodHelperOrig.signature.methodName))

      val classBaseFixed = NameFix.nameFixModular(classBase.resolveNamesModular, classBaseTransformed, baseDependencies)
      val fixedBaseGraph = classBaseFixed.resolveNamesModular

      // Conflict has been fixed
      assert(!fixedBaseGraph.E.contains(methodHelperOrig.signature.methodName))
      assert(!fixedBaseGraph.E.contains(methodHelperSynth.signature.methodName))
      assert(fixedBaseGraph.E.count(_._2.contains(methodHelperOrig.signature.methodName)) == 1)
      assert(fixedBaseGraph.E.count(_._2.contains(methodHelperSynth.signature.methodName)) == 1)
      // Check if public method was not renamed
      val methodHelperSynthFixed = classBaseTransformed.methods.find(m => m.signature.methodName.name == "methodHelper" && m.signature.accessModifier == AccessModifier.PUBLIC)
      assert(methodHelperSynthFixed.isDefined)

    case Parser.NoSuccess(msg, _) => fail(msg)
  })
}
