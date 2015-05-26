package lang.java

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

class NameGraphExtractor(originTrackedNames: Map[JCTree, Name]) extends TreeScanner[Void,Void] with NameVisitor[Void, Void] {
  private var _names = Set[Name.ID]()
  private var _edges = Map[Name.ID, Name.ID]()
  var symMap = Map[Symbol, Name]()
  var nodeMap = Map[JCTree, Name]()

  def names = _names
  def edges = _edges
  
  private def addDec(node: JCTree, sym: Symbol): Unit =
    originTrackedNames.get(node) match {
      case Some(jn@JName(name, _)) =>
        val n = new JName(name, node, jn.id)
        symMap += sym -> n
        nodeMap += node -> n
        _names += n.id
      case None =>
        symMap.get(sym) match {
          case Some(JName(_,wasnode)) => if (node != wasnode) throw new IllegalStateException(s"Symbol $sym defined in multiple nodes:\n$node\n$wasnode")
          case Some(name@Name(_)) =>
            val n = new JName(name.name, node, name.id)
            symMap += sym -> n
            nodeMap += node -> n
          case None =>
            val n = JName(sym.name.toString, node)
            symMap += sym -> n
            nodeMap += node -> n
            _names += n.id
        }
    }


  private def addRef(refnode: JCTree, sym: Symbol): Unit = {
    val dec = symMap.get(sym) match {
      case Some(n) => n
      case None => // external name or name that comes later in the tree
        val n = Name(sym.name.toString)
        symMap += sym -> n
        _names += n.id
        n
    }
    val ref = originTrackedNames.get(refnode) match {
      case Some(jn@JName(name, _)) =>
        val n = new JName(name, refnode, jn.id)
        nodeMap += refnode -> n
        _names += n.id
        n
      case None =>
        JName(sym.name.toString, refnode)
    }
    _names += ref.id
    nodeMap += refnode -> ref
    _edges += ref.id -> dec.id
  }

  override def visitClassDecl(node : JCClassDecl, p : Void) = addDec(node, node.sym)
  override def visitMethodDecl(node: JCMethodDecl, p: Void) = addDec(node, node.sym)
  override def visitVariableDecl(node: JCVariableDecl, p: Void) = addDec(node, node.sym)
  override def visitFieldAccess(node: JCFieldAccess, p: Void) = addRef(node, node.sym)
  override def visitIdentifierAccess(node: JCIdent, p: Void) = addRef(node, node.sym)
}
