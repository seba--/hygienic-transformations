package lang.java

import java.io.File

import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.code.Symbol.ClassSymbol
import com.sun.tools.javac.tree.JCTree.{JCClassDecl, JCCompilationUnit}
import com.sun.tools.javac.tree.{JCTree, TreeMaker}
import com.sun.tools.javac
import com.sun.tools.javac.util.{Context, Log}
import name.namegraph.NameGraphModular
import name._

case class TreeUnitInterface(moduleID: Identifier, export: Set[Identifier], name: javac.util.Name, sym: ClassSymbol) extends NameInterface {
  def rename(renaming: Renaming) = TreeUnitInterface(moduleID, export map(renaming(_)), name, sym)
}

class TreeUnit(val unit: JCCompilationUnit, val context: Context, originTrackedNames: Map[JCTree, Identifier] = Map()) extends NominalModular[TreeUnitInterface] {

  private var _resolved: NameGraphModular[TreeUnitInterface] = _
  var symMap: Map[Symbol, Identifier] = _
  var nodeMap: Map[JCTree, Identifier] = _

  var deps = Set[TreeUnitInterface]()
  override def link(deps: Set[TreeUnitInterface]) = {
    this.deps = deps
    _resolved = null
    this
  }

  private var _interface: TreeUnitInterface = _
  def interface = _interface

  def resolveNamesModular: NameGraphModular[TreeUnitInterface] = {
    if (_resolved != null)
      return _resolved

    val classes = (deps map (i => i.name -> i.sym)).toMap
    Java.analyzeTrees(List(unit), context, classes)
    val visitor = new NameGraphModularExtractor(deps, originTrackedNames)
    unit.accept(visitor, null)
    symMap = visitor.symMap
    nodeMap = visitor.nodeMap

    _interface = TreeUnitInterface(symMap(moduleSym), visitor.exported, moduleSym.name, moduleSym)
    _resolved = NameGraphModular(visitor.names, visitor.depsUsed, visitor.edges, interface)
    _resolved
  }

  private def moduleSym = unit.getTypeDecls.get(0) match {
    case cl: JCClassDecl => cl.sym
  }

  def allNames: Set[Name] = _resolved.V map (_.name)

  def rename(renaming: Renaming) = {
    val visitor = new RenameVisitor[Void](renaming, nodeMap, TreeMaker.instance(context))
    transform(visitor, null)
  }

  def transform[P](visitor: TrackingTreeCopier[P], p: P): TreeUnit = {
    val newUnit = unit.accept(visitor, p).asInstanceOf[JCCompilationUnit]
    newUnit.sourcefile = unit.sourcefile

    val originTrackedNames = visitor.originMap.flatMap{kv =>
      val oldName = nodeMap.get(kv._2)
      if (!oldName.isDefined)
        None
      else oldName match {
        case Some(jn@JName(_, _)) => Some(kv._1 -> new JName(jn.name, kv._1, jn))
        case Some(n) => Some(kv._1 -> n)
      }
    }
    TreeUnit.fromTrees(newUnit, context, originTrackedNames)
  }

  def silent[T](f: => T) = {
    val log = Log.instance(context)
    val nerrors = log.nerrors
    val nwarnings = log.nwarnings
    log.nerrors = Int.MaxValue
    log.nwarnings = Int.MaxValue
    try {
      f
    } finally {
      log.nerrors = nerrors
      log.nwarnings = nwarnings
    }
  }

  override def toString = unit.toString
}



object TreeUnit {
  def fromSourceFiles(sourceFile: File): TreeUnit = {
    val (units, context) = Java.parseSourceFiles(Seq(sourceFile))
    new TreeUnit(units.head, context)
  }

  def fromSourceCode(sourceFileCode/* name -> code*/: (String, String)): TreeUnit = {
    TreeUnit.fromSourceFiles(Java.makeTemporarySourceFiles(Map(sourceFileCode)).head)
  }

  def fromTrees(newUnit: JCCompilationUnit, context: Context, originTrackedNames: Map[JCTree, Identifier]): TreeUnit = {
    new TreeUnit(newUnit, context, originTrackedNames)
  }
}