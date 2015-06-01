package lang.java

import java.io.File

import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit
import com.sun.tools.javac.tree.{TreeMaker, TreeCopier, JCTree}
import com.sun.tools.javac.util.{Context, Log}
import name.namegraph.NameGraphExtended
import name.{Renaming, Identifier, Name, Nominal}

class Tree(val units: List[JCCompilationUnit], val context: Context, originTrackedNames: Map[JCTree, Identifier] = Map()) extends Nominal {

  lazy val (_resolveNames, symMap, nodeMap): (NameGraphExtended, Map[Symbol, Identifier], Map[JCTree, Identifier]) = {
    val visitor = new NameGraphExtractor(originTrackedNames)
    for (unit <- units)
      unit.accept(visitor, null)
    (NameGraphExtended(visitor.names, visitor.edges), visitor.symMap, visitor.nodeMap)
  }

  def resolveNames = _resolveNames
  def allNames: Set[Name] = resolveNames.V map (_.name)

  def rename(renaming: Renaming): Nominal = {
    val visitor = new RenameVisitor[Void](renaming, nodeMap, TreeMaker.instance(context))
    transform(visitor, null)
  }

  def transform[P](visitor: TrackingTreeCopier[P], p: P): Tree = {
    var newUnits = List[JCCompilationUnit]()
    for (unit <- units) {
      val newUnit = unit.accept(visitor, p).asInstanceOf[JCCompilationUnit]
      newUnit.sourcefile = unit.sourcefile
      newUnits = newUnits :+ newUnit
    }
    val originTrackedNames = visitor.originMap.flatMap{kv =>
      val oldName = nodeMap.get(kv._2)
      if (!oldName.isDefined)
        None
      else oldName match {
        case Some(jn@JName(_, _)) => Some(kv._1 -> new JName(jn.name, kv._1, jn))
        case Some(n) => Some(kv._1 -> n)
      }
    }
    Tree.fromTrees(newUnits, context, originTrackedNames)
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

  override def toString = units.mkString("\n")
}


object Tree {
  def fromSourceFiles(sourceFiles: Seq[File]): Tree = {
    val (units, context) = Java.parseSourceFiles(sourceFiles)
    Java.analyzeTrees(units, context)
    new Tree(units, context)
  }

  def fromSourceCode(sourceFileCodes/* name -> code*/: Map[String, String]): Tree = {
    Tree.fromSourceFiles(Java.makeTemporarySourceFiles(sourceFileCodes))
  }

  def fromTrees(newUnits: List[JCCompilationUnit], context: Context, originTrackedNames: Map[JCTree, Identifier]): Tree = {
    Java.analyzeTrees(newUnits, context)
    new Tree(newUnits, context, originTrackedNames)
  }
}