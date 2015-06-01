package lang.lightweightjava.modular

import lang.lightweightjava.{ClassInterface, Parser}
import name.namegraph.NameGraphModular
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

   "Modular Name Graph" should "contain 16 nodes, 11 internal edges, and 0 external edges for the class with no dependencies" in (Parser.parseAll(Parser.classDef, p1) match {
     case Parser.Success(p, _) =>
       val nameGraph = p.resolveNamesModular
       nameGraph.V.size should be (16)
       intEdges(nameGraph) should be (11)
       extEdges(nameGraph) should be (0)
       nameGraph.E.values.flatten.size should be (11)
       nameGraph.I.exportedFields.size should be (1)
       nameGraph.I.exportedMethods.size should be (1)
     case Parser.NoSuccess(msg, _) => fail(msg)
   })
  it should "contain 15 nodes, 6 internal edges and 0 external edges for the class with unresolved dependencies" in (Parser.parseAll(Parser.classDef, p2) match {
    case Parser.Success(p, _) =>
      val nameGraph = p.resolveNamesModular
      nameGraph.V.size should be (15)
      intEdges(nameGraph) should be (6)
      extEdges(nameGraph) should be (0)
      nameGraph.I.exportedFields.size should be (0)
      nameGraph.I.exportedMethods.size should be (1)
    case Parser.NoSuccess(msg, _) => fail(msg)
  })
  it should "contain 15 nodes, 7 internal edges and 7 external edges for the class with resolved dependencies" in ((Parser.parseAll(Parser.classDef, p1), Parser.parseAll(Parser.classDef, p2)) match {
    case (Parser.Success(x, _), Parser.Success(y, _)) =>
      val nameGraphX = x.resolveNamesModular
      val nameGraphY = y.link(nameGraphX.I).resolveNamesModular
      nameGraphY.V.size should be (15)
      intEdges(nameGraphY) should be (7)
      extEdges(nameGraphY) should be (7)
      nameGraphY.I.exportedFields.size should be (0)
      nameGraphY.I.exportedMethods.size should be (1)
    case _ => fail("Parsing error!")
  })


  protected def intEdges(g: NameGraphModular[ClassInterface]): Int = {
    g.E.map(_._2.intersect(g.V).size).sum
  }

  protected def extEdges(g: NameGraphModular[ClassInterface]): Int = {
    g.E.map(_._2.diff(g.V).size).sum
  }
 }