package lang.java

import com.sun.source.tree._
import com.sun.source.util.TreeScanner
import com.sun.tools.javac.tree.JCTree._

trait NameVisitor[R,P] extends TreeScanner[R, P] {
  def visitClassDecl(node: JCClassDecl, p: P)
  def visitMethodDecl(node: JCMethodDecl, p: P)
  // field decls, param decls, local var decls
  def visitVariableDecl(node: JCVariableDecl, p: P)
  def visitFieldAccess(node: JCFieldAccess, p: P)
  def visitIdentifierAccess(node: JCIdent, p: P)

  override def visitClass(node : ClassTree, p : P) : R = node match {
    case n: JCClassDecl =>
      visitClassDecl(n, p)
      super.visitClass(node, p)
  }

  override def visitMethod(node: MethodTree, p: P): R = node match {
    case n: JCMethodDecl =>
      visitMethodDecl(n, p)
      super.visitMethod(node, p)
  }

  // field decls, param decls, local var decls
  override def visitVariable(node: VariableTree, p: P): R = node match {
    case n: JCVariableDecl =>
      visitVariableDecl(n, p)
      super.visitVariable(node, p)
  }

  override def visitMemberSelect(node: MemberSelectTree, p: P): R = node match {
    case n: JCFieldAccess =>
      visitFieldAccess(n, p)
      super.visitMemberSelect(node, p)
  }

  override def visitIdentifier(node: IdentifierTree, p: P): R = node match {
    case n: JCIdent =>
      visitIdentifierAccess(n, p)
      super.visitIdentifier(node, p)
  }
}
