package lang.lightweightjava

import lang.lightweightjava.ast.AccessModifier
import lang.lightweightjava.ast.returnvalue.{ReturnField, ReturnMethodCall}
import lang.lightweightjava.ast.statement.{MethodCall, VoidMethodCall}
import lang.lightweightjava.trans.privatefield.MakeFieldPrivateTransformation
import name.namefix.NameFix
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

  // Dummy class for LJ integers
  val intClass =
    "class int {" +
    "  public int negate() {" +
    "    return new int();" +
    "  }" +
    "  public int substract(int other) {" +
    "    return new int();" +
    "  }" +
    "}"

  val pointClass =
    "class Point {" +
    "  public int x;" +
    "  public int y;" +
    "}"
  val mirroredPointClass =
    "class MirroredPoint extends Point {" +
      "  public int getY() {" +
      "    int y;" +
      "    y = this.y;" +
      "    return y.negate();" +
      "  }" +
      "}"
  val pointUtilClass =
    "class PointUtil {" +
      "  public int compareY(MirroredPoint a, MirroredPoint b) {" +
      "    int aY;" +
      "    int bY;" +
      "    aY = a.getY();" +
      "    bY = b.getY();" +
      "    return aY.substract(bY);" +
      "  }" +
      "}"

  "NameFix" should "fix the capture resulting for the added getters/setters" in (Parser.parseAll(Parser.program, intClass + pointClass + mirroredPointClass) match {
    case Parser.Success(p, _) =>
      val classInt = p.findClassDefinition("int").get
      val classPoint = p.findClassDefinition("Point").get
      val classMirroredPoint = p.findClassDefinition("MirroredPoint").get
      val fieldX = classPoint.fields.find(_.fieldName.name == "x").get
      val fieldY = classPoint.fields.find(_.fieldName.name == "y").get
      val classPointIntermediate = MakeFieldPrivateTransformation.transform(classPoint, fieldX.fieldName, p)
      val classPointTransformed = MakeFieldPrivateTransformation.transform(classPointIntermediate, fieldY.fieldName, p)
      val classMirroredPointIntermediate = MakeFieldPrivateTransformation.transform(classMirroredPoint, fieldX.fieldName, p)
      val classMirroredPointTransformed = MakeFieldPrivateTransformation.transform(classMirroredPointIntermediate, fieldY.fieldName, p)

      val pointYGetter = classPointTransformed.methods.find(_.signature.methodName.name == "getY").get
      val mirroredPointYGetter = classMirroredPointTransformed.methods.find(_.signature.methodName.name == "getY").get

      classMirroredPoint.link(Set(classPoint.interface, classInt.interface))
      val originalGraph = classMirroredPoint.resolveNamesModular

      classMirroredPointTransformed.link(Set(classPointTransformed.interface, classInt.interface))
      val transformedGraph = classMirroredPointTransformed.resolveNamesModular

      // Capture through inheritance
      assert(transformedGraph.E(mirroredPointYGetter.signature.methodName).contains(pointYGetter.signature.methodName))

      val classMirroredPointFixed = NameFix.nameFixModular(originalGraph, classMirroredPointTransformed, Set(classPointTransformed.interface, classInt.interface))
      val fixedGraph = classMirroredPointFixed.resolveNamesModular
      assert(!fixedGraph.E.contains(mirroredPointYGetter.signature.methodName))
      assert(fixedGraph.E.exists(_._2.contains(pointYGetter.signature.methodName)))

      info("Transformed class MirroredPoint: " + classMirroredPointTransformed.toString)
      info("Fixed class MirroredPoint: " + classMirroredPointFixed.toString)

    case Parser.NoSuccess(msg, _) => fail(msg)
  })

  "NameFix" should "propagate the renaming of public names to the dependent class" in (Parser.parseAll(Parser.program, intClass + pointClass + mirroredPointClass + pointUtilClass) match {
    case Parser.Success(p, _) =>
      val classInt = p.findClassDefinition("int").get
      val classPoint = p.findClassDefinition("Point").get
      val classMirroredPoint = p.findClassDefinition("MirroredPoint").get
      val classPointUtil = p.findClassDefinition("PointUtil").get
      val fieldX = classPoint.fields.find(_.fieldName.name == "x").get
      val fieldY = classPoint.fields.find(_.fieldName.name == "y").get
      val classPointIntermediate = MakeFieldPrivateTransformation.transform(classPoint, fieldX.fieldName, p)
      val classPointTransformed = MakeFieldPrivateTransformation.transform(classPointIntermediate, fieldY.fieldName, p)
      val classMirroredPointIntermediate = MakeFieldPrivateTransformation.transform(classMirroredPoint, fieldX.fieldName, p)
      val classMirroredPointTransformed = MakeFieldPrivateTransformation.transform(classMirroredPointIntermediate, fieldY.fieldName, p)

      val pointYGetter = classPointTransformed.methods.find(_.signature.methodName.name == "getY").get
      val mirroredPointYGetter = classMirroredPointTransformed.methods.find(_.signature.methodName.name == "getY").get

      classMirroredPoint.link(Set(classPoint.interface, classInt.interface))
      val originalGraph = classMirroredPoint.resolveNamesModular

      classMirroredPointTransformed.link(Set(classPointTransformed.interface, classInt.interface))

      val classMirroredPointFixed = NameFix.nameFixModular(originalGraph, classMirroredPointTransformed, Set(classPointTransformed.interface, classInt.interface))
      val fixedGraph = classMirroredPointFixed.resolveNamesModular

      classPointUtil.link(Set(classMirroredPoint.interface, classInt.interface))
      val originalPUGraph = classPointUtil.resolveNamesModular

      classPointUtil.link(Set(classMirroredPointFixed.interface, classInt.interface))
      val unpropagatedPUGraph = classPointUtil.resolveNamesModular

      // Capture: The unpropagated name graph of PointUtil points to the getters of Point as MirroredPoint was renamed
      assert(unpropagatedPUGraph.E.exists(_._2.contains(pointYGetter.signature.methodName)))
      assert(!unpropagatedPUGraph.E.exists(_._2.contains(mirroredPointYGetter.signature.methodName)))

      // As PointUtil was not actually transformed, the source and target name graphs are identical
      val classPointUtilFixed = NameFix.nameFixModular(originalPUGraph, classPointUtil, Set(classMirroredPointFixed.interface, classInt.interface))
      val propagatedGraph = classPointUtilFixed.resolveNamesModular

      assert(!propagatedGraph.E.exists(_._2.contains(pointYGetter.signature.methodName)))
      assert(propagatedGraph.E.exists(_._2.contains(mirroredPointYGetter.signature.methodName)))

      info("Unpropagated class PointUtil: " + classPointUtil.toString)
      info("Propagated class PointUtil: " + classPointUtilFixed.toString)

    case Parser.NoSuccess(msg, _) => fail(msg)
  })
}
