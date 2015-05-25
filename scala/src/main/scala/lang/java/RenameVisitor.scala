package lang.java

import com.sun.source.tree._
import com.sun.tools.javac.tree.{TreeMaker, TreeCopier, JCTree}
import com.sun.tools.javac.tree.JCTree._
import com.sun.tools.javac.util
import name.Name

class RenameVisitor[P](renaming: Name => Name, nodeMap: Map[JCTree, Name], tm: TreeMaker) extends TreeCopier[P](tm) {
  
  def rename(node: JCTree, nodeName: util.Name) = nodeMap.get(node) match {
    case None => nodeName
    case Some(name) =>
      val newname = renaming(name).name
      if (newname != name.name)
        nodeName.table.fromString(newname)
      else
        nodeName
  }

  override def visitClass(node : ClassTree, p : P) : JCTree = node match {
    case n: JCClassDecl =>
      val mods = this.copy(n.mods, p)
      val typarams = this.copy(n.typarams, p)
      val extending = this.copy(n.extending, p)
      val implementing = this.copy(n.implementing, p)
      val defs = this.copy(n.defs, p)
      val newName = rename(n, n.name)
      tm.at(n.pos).ClassDef(mods, newName, typarams, extending, implementing, defs)
  }

  override def visitMethod(node: MethodTree, p: P): JCTree = node match {
    case n: JCMethodDecl =>
      val mods = this.copy(n.mods, p)
      val restype = this.copy(n.restype, p)
      val typarams = this.copy(n.typarams, p)
      val params = this.copy(n.params, p)
      val thrown = this.copy(n.thrown, p)
      val body = this.copy(n.body, p)
      val defaultValue = this.copy(n.defaultValue, p)
      val newName = rename(n, n.name)
      tm.at(n.pos).MethodDef(mods, newName, restype, typarams, params, thrown, body, defaultValue)
  }

  // field decls, param decls, local var decls
  override def visitVariable(node: VariableTree, p: P): JCTree = node match {
    case n: JCVariableDecl =>
      val mods = this.copy(n.mods, p)
      val vartype = this.copy(n.vartype, p)
      val init = this.copy(n.init, p)
      val newName = rename(n, n.name)
      tm.at(n.pos).VarDef(mods, newName, vartype, init)
  }

  override def visitMemberSelect(node: MemberSelectTree, p: P): JCTree = node match {
    case n: JCFieldAccess =>
      val selected = this.copy(n.selected, p)
      val newName = rename(n, n.name)
      tm.at(n.pos).Select(selected, newName);
  }

  override def visitIdentifier(node: IdentifierTree, p: P): JCTree = node match {
    case n: JCIdent =>
      val newName = rename(n, n.name)
      tm.at(n.pos).Ident(newName)
  }
}
