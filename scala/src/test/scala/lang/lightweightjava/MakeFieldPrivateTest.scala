package lang.lightweightjava

import lang.lightweightjava.ast.AccessModifier
import lang.lightweightjava.ast.returnvalue.{ReturnField, ReturnMethodCall}
import lang.lightweightjava.ast.statement.{MethodCall, VoidMethodCall}
import lang.lightweightjava.privatefield.MakeFieldPrivateTransformation
import org.scalatest.{FlatSpec, Matchers}

class MakeFieldPrivateTest extends FlatSpec with Matchers {
  val c1 =
    "class A {\n" +
      "  public Object x;\n" +
      "  public A other;\n" +
  "}\n"
  val c1extended =
    "class A {\n" +
      "  public A x;\n" +
      "  public A other;\n" +
      "  public Object method(A a) {\n" +
      "    this.x = a;\n" +
      "    return a.x;\n" +
      "  }\n" +
  "}\n"
  val c2 =
    "class B {\n" +
      "  public Object x;\n" +
      "  public Object method(A a) {\n" +
      "    a = a.x;\n" +
      "    return this.x;\n" +
      "  }\n" +
      "}\n"

  "MakeFieldPrivateTransformation" should "replace the public fields in class A with getters and setters" in (Parser.parseAll(Parser.program, c1) match {
    case Parser.Success(p, _) =>
      val classA = p.findClassDefinition("A").get
      val fieldX = classA.fields.find(_.fieldName.name == "x").get
      val fieldOther = classA.fields.find(_.fieldName.name == "other").get
      val classAPrivateX = MakeFieldPrivateTransformation.transform(classA, fieldX.fieldName, p)
      val classAPrivateBoth = MakeFieldPrivateTransformation.transform(classAPrivateX, fieldOther.fieldName, p)

      assert(classAPrivateBoth.fields.forall(_.accessModifier == AccessModifier.PRIVATE))
      assert(classAPrivateBoth.methods.exists(_.signature.methodName.name == "getX"))
      assert(classAPrivateBoth.methods.exists(_.signature.methodName.name == "setX"))
      assert(classAPrivateBoth.methods.exists(_.signature.methodName.name == "getOther"))
      assert(classAPrivateBoth.methods.exists(_.signature.methodName.name == "setOther"))
      info("Transformed class A: " + classAPrivateBoth.toString)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })

  "MakeFieldPrivateTransformation" should "replace the internal field read/writes with getter/setter calls" in (Parser.parseAll(Parser.program, c1extended) match {
    case Parser.Success(p, _) =>
      val classA = p.findClassDefinition("A").get
      val fieldX = classA.fields.find(_.fieldName.name == "x").get
      val fieldOther = classA.fields.find(_.fieldName.name == "other").get
      val classAPrivateX = MakeFieldPrivateTransformation.transform(classA, fieldX.fieldName, p)
      val classAPrivateBoth = MakeFieldPrivateTransformation.transform(classAPrivateX, fieldOther.fieldName, p)
      val methodTransformed = classAPrivateBoth.methods.find(_.signature.methodName.name == "method").get

      methodTransformed.methodBody.returnValue match {
        case ReturnMethodCall(source, method, param@_*) =>
          assert(source.name == "a")
          assert(method.name == "getX")
          assert(param.isEmpty)
        case _ => fail("Return not replaced by method call!")
      }

      methodTransformed.methodBody.statements.head match {
        case VoidMethodCall(source, method, param@_*) =>
          assert(source.name == "this")
          assert(method.name == "setX")
          assert(param.size == 1)
          assert(param.head.name == "a")
        case _ => fail("Field write not replaced by method call!")
      }

      info("Transformed class A: " + classAPrivateBoth.toString)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })

  "MakeFieldPrivateTransformation" should "replace the external field read/writes with getter/setter calls" in (Parser.parseAll(Parser.program, c1extended + c2) match {
    case Parser.Success(p, _) =>
      val classA = p.findClassDefinition("A").get
      val fieldX = classA.fields.find(_.fieldName.name == "x").get
      val fieldOther = classA.fields.find(_.fieldName.name == "other").get
      val classB = p.findClassDefinition("B").get
      val fieldBX = classB.fields.find(_.fieldName.name == "x").get
      val classBPrivateX = MakeFieldPrivateTransformation.transform(classB, fieldX.fieldName, p)
      val classBPrivateBoth = MakeFieldPrivateTransformation.transform(classBPrivateX, fieldOther.fieldName, p)
      val methodTransformed = classBPrivateBoth.methods.find(_.signature.methodName.name == "method").get

      methodTransformed.methodBody.returnValue match {
        case ReturnField(target, field) =>
          assert(target.name == "this")
          assert(field.name == "x")
        case _ => fail("Return was replaced by method call!")
      }

      methodTransformed.methodBody.statements.head match {
        case MethodCall(target, source, method, param@_*) =>
          assert(target.name == "a")
          assert(source.name == "a")
          assert(method.name == "getX")
          assert(param.isEmpty)
        case _ => fail("Field read not replaced by method call!")
      }

      info("Transformed class B: " + classBPrivateBoth.toString)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
}
