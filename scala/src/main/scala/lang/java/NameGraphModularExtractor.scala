package lang.java

import com.sun.source.util.TreeScanner
import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.code.Symbol.ClassSymbol
import com.sun.tools.javac.code.Type.ClassType
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree._
import name.Identifier


class NameGraphModularExtractor(deps: Set[TreeUnitInterface], originTrackedNames: Map[JCTree, Identifier]) extends TreeScanner[Void,Void] with NameVisitor[Void, Void] {
  import NameGraphExtractor.globalNames

  private var _names = Set[Identifier]()
  private var _edges = Map[Identifier, Set[Identifier]]()
  private var _symMap = globalNames
  private var _nodeMap = Map[JCTree, Identifier]()
  private var _depsUsed = Set[TreeUnitInterface]()
  private var _exported = Set[Identifier]()

  def names = _names
  def edges = _edges
  def symMap = {
    val (local, global) = _symMap.partition(kv => kv._2.isInstanceOf[JName])
    globalNames ++= global
    local
  }
  def nodeMap = _nodeMap
  def depsUsed = _depsUsed
  def exported = _exported
  
  private def addDec(node: JCTree, sym: Symbol): Unit =
    originTrackedNames.get(node) match {
      case Some(jn@JName(name, _)) =>
        val n = JName(name, node, jn)
        _symMap += sym -> n
        _nodeMap += node -> n
        _names += n
      case None =>
        _symMap.get(sym) match {
          case Some(JName(_,wasnode)) => if (node != wasnode) throw new IllegalStateException(s"Symbol $sym defined in multiple nodes:\n$node\n$wasnode")
          case Some(ident@Identifier(_)) =>
            val n = new JName(ident.name, node, ident)
            _symMap += sym -> n
            _nodeMap += node -> n
          case None =>
            val n = JName(sym.name.toString, node)
            _symMap += sym -> n
            _nodeMap += node -> n
            _names += n
        }
    }


  private def addRef(refnode: JCTree, sym: Symbol): Unit = {
    val dec = _symMap.get(sym) match {
      case Some(n) => n
      case None => // external name or name that comes later in the tree
        val n = Identifier(sym.name.toString)
        _symMap += sym -> n
        _names += n
        n
    }
    val ref = originTrackedNames.get(refnode) match {
      case Some(jn@JName(name, _)) =>
        val n = JName(name, refnode, jn)
        _nodeMap += refnode -> n
        _names += n
        n
      case None =>
        JName(sym.name.toString, refnode)
    }
    _names += ref
    _nodeMap += refnode -> ref
    _edges += ref -> (_edges.getOrElse(ref, Set[Identifier]()) + dec)
  }

  override def visitClassDecl(node : JCClassDecl, p : Void) = addDec(node, node.sym)
  override def visitMethodDecl(node: JCMethodDecl, p: Void) = {
    addDec(node, node.sym)
  // TODO method overriding
//    tryAddOverridingRef(node, node.sym)
  }
  override def visitVariableDecl(node: JCVariableDecl, p: Void) = addDec(node, node.sym)
  override def visitFieldAccess(node: JCFieldAccess, p: Void) = addRef(node, node.sym)
  override def visitIdentifierAccess(node: JCIdent, p: Void) = addRef(node, node.sym)

  def tryAddOverridingRef(node: JCTree, nodeSym: Symbol): Unit = {
    nodeSym.owner.`type` match {
      case sub: ClassType =>
        sub.supertype_field match {
          case sup: ClassType =>
            val it = sup.tsym.asInstanceOf[ClassSymbol].members_field.getElements.iterator()
            while (it.hasNext) {
              val nextSym = it.next()
              if (nextSym.name == nodeSym.name)
                addRef(node, nextSym)
            }
          case _ =>
        }
      case _ =>
    }
  }
}
