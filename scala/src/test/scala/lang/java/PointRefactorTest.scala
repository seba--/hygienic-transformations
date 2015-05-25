package lang.java

import java.io.{OutputStream, PrintWriter}

import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree._
import lang.java.trans.MakeFieldPrivate
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
      |  public void mirror() { this.y = -y; }
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

    val MirroredPoint_mirror = clazz_MirroredPoint.getMembers.get(2).asInstanceOf[JCMethodDecl]
    val MirroredPoint_mirror_ass = MirroredPoint_mirror.body.stats.get(0).asInstanceOf[JCExpressionStatement].expr.asInstanceOf[JCAssign]
    val MirroredPoint_mirror_getRef = MirroredPoint_mirror_ass.rhs.asInstanceOf[JCUnary].arg.asInstanceOf[JCIdent]
    val MirroredPoint_mirror_setRef = MirroredPoint_mirror_ass.lhs.asInstanceOf[JCFieldAccess]
  }

  case class RefactoredPointStuff(tree: Tree) {
    val PointUnit = tree.units.head
    val MirroredPointUnit = tree.units.tail.head
    val clazz_Point = PointUnit.getTypeDecls.get(0).asInstanceOf[JCClassDecl]
    val field_x = clazz_Point.getMembers.get(1).asInstanceOf[JCVariableDecl]
    val field_y = clazz_Point.getMembers.get(2).asInstanceOf[JCVariableDecl]
    val Point_getY = clazz_Point.getMembers.get(3).asInstanceOf[JCMethodDecl]
    private val Point_getY_ret = Point_getY.getBody.getStatements.get(0).asInstanceOf[JCReturn]
    val field_y_PointGetterReference = Point_getY_ret.getExpression.asInstanceOf[JCIdent]
    val Point_setY = clazz_Point.getMembers.get(4).asInstanceOf[JCMethodDecl]
    private val Point_setY_ass = Point_setY.getBody.getStatements.get(0).asInstanceOf[JCExpressionStatement].getExpression.asInstanceOf[JCAssign]
    val field_y_PointSetterReference = Point_setY_ass.lhs.asInstanceOf[JCFieldAccess]
    val var_y_PointSetterReference = Point_setY_ass.rhs.asInstanceOf[JCIdent]

    val clazz_MirroredPoint = MirroredPointUnit.getTypeDecls.get(0).asInstanceOf[JCClassDecl]
    val extending_Point = clazz_MirroredPoint.extending.asInstanceOf[JCIdent]
    val MirroredPoint_getY = clazz_MirroredPoint.getMembers.get(1).asInstanceOf[JCMethodDecl]
    private val MirroredPoint_getY_ret = MirroredPoint_getY.getBody.getStatements.get(0).asInstanceOf[JCReturn]
    val MirroredPoint_getY_ret_Point_getY_ref = MirroredPoint_getY_ret.getExpression.asInstanceOf[JCUnary].arg.asInstanceOf[JCMethodInvocation].meth.asInstanceOf[JCIdent]

    val MirroredPoint_mirror = clazz_MirroredPoint.getMembers.get(2).asInstanceOf[JCMethodDecl]
    val MirroredPoint_mirror_setCall = MirroredPoint_mirror.body.stats.get(0).asInstanceOf[JCExpressionStatement].expr.asInstanceOf[JCMethodInvocation]
    val MirroredPoint_mirror_getRef = MirroredPoint_mirror_setCall.args.get(0).asInstanceOf[JCUnary].arg.asInstanceOf[JCMethodInvocation].meth.asInstanceOf[JCIdent]
    val MirroredPoint_mirror_setRef = MirroredPoint_mirror_setCall.meth.asInstanceOf[JCIdent]
  }

  test("original bindings") {
    val points = PointStuff(originalTree)
    import points._
    assertEdge(tree, field_y_getterReference -> field_y, field_y.sym)
    assertEdge(tree, MirroredPoint_mirror_getRef -> field_y, field_y.sym)
    assertEdge(tree, MirroredPoint_mirror_setRef -> field_y, field_y.sym)
  }

  test("refactored non-fixed bindings") {
    val points = PointStuff(originalTree)
    val refactoredTree = MakeFieldPrivate(points.field_y.sym, originalTree)

    val rpoints = RefactoredPointStuff(refactoredTree)
    import rpoints._

    assertEdge(refactoredTree, field_y_PointGetterReference -> field_y, field_y.sym)
    assertEdge(refactoredTree, field_y_PointSetterReference -> field_y, field_y.sym)
    assertNotEdge(refactoredTree, var_y_PointSetterReference -> field_y)

    // this is a captured edge, `MirroredPoint_getY_ret_Point_getY_ref` should refer to Point.getY()
    assertEdge(refactoredTree, MirroredPoint_getY_ret_Point_getY_ref -> MirroredPoint_getY, MirroredPoint_getY.sym)
    // this is a captured edge, `MirroredPoint_mirror_getRef` should refer to Point.getY()
    assertEdge(tree, MirroredPoint_mirror_getRef -> MirroredPoint_getY, MirroredPoint_getY.sym)

    assertEdge(tree, MirroredPoint_mirror_setRef -> Point_setY, Point_setY.sym)
  }

}