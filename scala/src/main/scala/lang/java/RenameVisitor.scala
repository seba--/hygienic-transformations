package lang.java

import com.sun.source.tree.IdentifierTree
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree._
import com.sun.tools.javac.util
import name.Name

class RenameVisitor(renaming: Name => Name, nodeMap: Map[JCTree, Name]) extends NameVisitor[Void,Void] {
  
  case class NameObject(get: () => util.Name, set: util.Name => Unit)
  
  def renameNode(node: JCTree, nodeName: NameObject) = nodeMap.get(node) match {
    case None => // do nothing
    case Some(name) =>
      val newname = renaming(name).name
      if (newname != name.name) {
        val newTableName = nodeName.get().table.fromString(newname)
        nodeName.set(newTableName)
      }
  }
  
  def visitClassDecl(node: JCClassDecl, p: Void) = renameNode(node, NameObject(() => node.name, n => node.name = n))
  def visitFieldAccess(node: JCFieldAccess, p: Void) = renameNode(node, NameObject(() => node.name, n => node.name = n))
  def visitIdentifierAccess(node: JCIdent, p: Void) = renameNode(node, NameObject(() => node.name, n => node.name = n))
  def visitMethodDecl(node: JCMethodDecl, p: Void) = renameNode(node, NameObject(() => node.name, n => node.name = n))
  def visitVariableDecl(node: JCVariableDecl, p: Void) = renameNode(node, NameObject(() => node.name, n => node.name = n))
}
