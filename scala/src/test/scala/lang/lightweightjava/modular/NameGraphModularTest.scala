package lang.lightweightjava.modular

import lang.lightweightjava.Parser
import org.scalatest.{FlatSpec, Matchers}

class NameGraphModularTest extends FlatSpec with Matchers {
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
  val p2 =
    "class Y extends X {\n" +
      "   X m2(X var1, X var2) {\n" +
      "     if (var1 == var2)\n" +
      "       var1 = new X();\n" +
      "     else {\n" +
      "       var2 = var1.field;\n" +
      "     }\n" +
      "     return null;\n" +
      "   }\n" +
      "}\n"

   "Name Graph" should "contain 16 nodes, 11 internal edges from 11 nodes, and 0 external edges for the class with no dependencies" in (Parser.parseAll(Parser.classDef, p1) match {
     case Parser.Success(p, _) =>
       val (nameGraph, meta) = p.resolveNamesModular()
       nameGraph.V.size should be (16)
       nameGraph.E.size should be (11)
       nameGraph.EOut.size should be (0)
       nameGraph.E.values.flatten.size should be (11)
       meta.exportedFields.size should be (1)
       meta.exportedMethods.size should be (1)
     case Parser.NoSuccess(msg, _) => fail(msg)
   })
  it should "contain 15 nodes, 6 internal edges from 6 nodes, and 0 external edges for the class with unresolved dependencies" in (Parser.parseAll(Parser.classDef, p2) match {
    case Parser.Success(p, _) =>
      val (nameGraph, meta) = p.resolveNamesModular()
      nameGraph.V.size should be (15)
      nameGraph.E.size should be (6)
      nameGraph.EOut.size should be (0)
      nameGraph.E.values.flatten.size should be (6)
      meta.exportedFields.size should be (0)
      meta.exportedMethods.size should be (1)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain 15 nodes, 6 internal edges from 6 nodes, and 7 external edges from 7 nodes for the class with resolved dependencies" in ((Parser.parseAll(Parser.classDef, p1), Parser.parseAll(Parser.classDef, p2)) match {
    case (Parser.Success(x, _), Parser.Success(y, _)) =>
      val (_, metaX) = x.resolveNamesModular()
      val (nameGraphY, metaY) = y.resolveNamesModular(Set(metaX))
      nameGraphY.V.size should be (15)
      nameGraphY.E.size should be (6)
      nameGraphY.EOut.size should be (7)
      nameGraphY.E.values.flatten.size should be (6)
      nameGraphY.EOut.values.flatten.size should be (7)
      metaY.exportedFields.size should be (0)
      metaY.exportedMethods.size should be (1)
    case _ => fail("Parsing error!")
  })
 }