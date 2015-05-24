package lang.java

import java.io.{OutputStream, PrintWriter}

import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree._
import org.scalatest._

class PointRefactorTest extends FunSuite {

  def assertEdge(tree: Tree, refDec: (JCTree, JCTree), sym: Symbol = null): Unit = {
    val ref = refDec._1
    val dec = refDec._2
    if (sym != null)
      assert(tree.symMap(sym).id == tree.nodeMap(dec).id)
    assert(tree.nodeMap(dec).id != tree.nodeMap(ref).id)
    assert(tree.nodeMap(dec).id == tree.resolveNames.E(tree.nodeMap(ref).id))
  }

  def assertNotEdge(tree: Tree, refDec: (JCTree, JCTree)): Unit = {
    val ref = refDec._1
    val dec = refDec._2
    assert(tree.nodeMap(dec).id != tree.nodeMap(ref).id)
    assert(tree.nodeMap(dec).id != tree.resolveNames.E(tree.nodeMap(ref).id))
  }

  val pointCode =
    """
      |public class Point {
      |  public int x, y;
      |}
    """.stripMargin

  val mirroredPointCode =
    """
      |public class MirroredPoint extends Point {
      |  public int getY() { return -y; }
      |}
    """.stripMargin

  val originalTree = Tree.fromSourceCode(Map("Point" -> pointCode, "MirroredPoint" -> mirroredPointCode))
  case class PointStuff(tree: Tree) {
    val PointUnit = tree.units.head
    val MirroredPointUnit = tree.units.tail.head
    val clazz_Point = PointUnit.getTypeDecls.get(0).asInstanceOf[JCClassDecl]
    val field_x = clazz_Point.getMembers.get(1).asInstanceOf[JCVariableDecl]
    val field_y = clazz_Point.getMembers.get(2).asInstanceOf[JCVariableDecl]

    val clazz_MirroredPoint = MirroredPointUnit.getTypeDecls.get(0).asInstanceOf[JCClassDecl]
    val extending_Point = clazz_MirroredPoint.extending.asInstanceOf[JCIdent]
    val MirroredPoint_getY = clazz_MirroredPoint.getMembers.get(1).asInstanceOf[JCMethodDecl]
    private val MirroredPoint_getY_ret = MirroredPoint_getY.getBody.getStatements.get(0).asInstanceOf[JCReturn]
    val field_y_getterReference = MirroredPoint_getY_ret.getExpression.asInstanceOf[JCUnary].arg.asInstanceOf[JCIdent]
  }

  test("original bindings") {
    val points = PointStuff(originalTree)
    import points._
    assertEdge(tree, field_y_getterReference -> field_y, field_y.sym)
  }



}