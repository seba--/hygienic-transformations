package lang.java

import com.sun.source.tree._
import com.sun.source.util.TreeScanner
import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree._
import name.Name

class JName(name: String, val node: JCTree, id: Name.ID) extends Name(name, id)
object JName {
  def apply(name: String, node: JCTree) = {
    val n = Name(name)
    val jn = new JName(name, node, n.id)
    n.id.nameO = jn
    jn
  }

  def unapply(n: JName): Option[(String, JCTree)] = Some((n.name, n.node))
}

class NameGraphExtractor extends TreeScanner[Void, Void] {
  var names = Set[Name.ID]()
  var edges = Map[Name.ID, Name.ID]()
  var symMap = Map[Symbol, Name]()

  private def addDef(node: JCTree, sym: Symbol): Unit = {
    val dec = symMap.get(sym) match {
      case Some(JName(_,wasnode)) => if (node != wasnode) throw new IllegalStateException(s"Symbol $sym defined in multiple nodes:\n$node\n$wasnode")
      case Some(name@Name(_)) =>
        val n = new JName(name.name, node, name.id)
        symMap += sym -> n
      case None =>
        val n = JName(sym.name.toString, node)
        symMap += sym -> n
        names += n.id
    }
  }

  private def addUse(refnode: JCTree, sym: Symbol): Unit = {
    val ref = JName(sym.name.toString, refnode)
    val dec = symMap.get(sym) match {
      case Some(n) => n
      case None => // external name or name that comes later in the tree
        val n = Name(sym.name.toString)
        symMap += sym -> n
        names += n.id
        n
    }
    names += ref.id
    edges += ref.id -> dec.id
  }




  override def visitClass(node : ClassTree, p : Void) : Void = node match {
    case n: JCClassDecl =>
      addDef(n, n.sym)
      super.visitClass(node, p)
  }

  override def visitMethod(node: MethodTree, p: Void): Void = node match {
    case n: JCMethodDecl =>
      addDef(n, n.sym)
      super.visitMethod(node, p)
  }

  override def visitVariable(node: VariableTree, p: Void): Void = node match {
    case n: JCVariableDecl =>
      addDef(n, n.sym)
      super.visitVariable(node, p)
  }

  override def visitEnhancedForLoop(node : EnhancedForLoopTree, p : Void) : Void = ???

  override def visitForLoop(node : ForLoopTree, p : Void) : Void = ???

  override def visitMemberSelect(node: MemberSelectTree, p: Void): Void = node match {
    case n: JCFieldAccess =>
      addUse(n, n.sym)
      super.visitMemberSelect(node, p)
  }

  override def visitIdentifier(node: IdentifierTree, p: Void): Void = node match {
    case n: JCIdent =>
      addUse(n, n.sym)
      super.visitIdentifier(node, p)
  }
}
