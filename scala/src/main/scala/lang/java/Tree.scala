package lang.java

import java.io.File

import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit
import com.sun.tools.javac.tree.{TreeMaker, TreeCopier, JCTree}
import com.sun.tools.javac.util.{Context, Log}
import name.{NameGraph, Name, Nominal}

class Tree(val units: List[JCCompilationUnit], val context: Context, originTrackedNames: Map[JCTree, Name] = Map()) extends Nominal {

  lazy val (_resolveNames, symMap, nodeMap): (NameGraph, Map[Symbol, Name], Map[JCTree, Name]) = {
    val visitor = new NameGraphExtractor(originTrackedNames)
    for (unit <- units)
      unit.accept(visitor, null)
    (NameGraph(visitor.names, visitor.edges), visitor.symMap, visitor.nodeMap)
  }

  def resolveNames = _resolveNames
  def allNames: Set[Name.ID] = resolveNames.V

  def rename(renaming: Renaming): Nominal = {
    val visitor = new RenameVisitor[Void](renaming, nodeMap, TreeMaker.instance(context))
    transform(visitor, null)
  }

  def transform[P](visitor: TrackingTreeCopier[P], p: P) = {
    var newUnits = List[JCCompilationUnit]()
    for (unit <- units) {
      val newUnit = unit.accept(visitor, p).asInstanceOf[JCCompilationUnit]
      newUnit.sourcefile = unit.sourcefile
      newUnits = newUnits :+ newUnit
    }
    val originTrackedNames = visitor.originMap.flatMap(kv => nodeMap.get(kv._2) map (kv._1 -> _))
    Tree.fromTrees(newUnits, context, originTrackedNames)
  }

  def silent[T](f: => T) = {
    val log = Log.instance(context)
    val nerrors = log.nerrors
    val nwarnings = log.nwarnings
    log.nerrors = log.MaxErrors
    log.nwarnings = log.MaxWarnings
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
    val (units, context) = Java.analyzeSourceFiles(sourceFiles)
    new Tree(units, context)
  }

  def fromSourceCode(sourceFileCodes/* name -> code*/: Map[String, String]): Tree = {
    Tree.fromSourceFiles(Java.makeTemporarySourceFiles(sourceFileCodes))
  }

  def fromTrees(newUnits: List[JCCompilationUnit], context: Context, originTrackedNames: Map[JCTree, Name]): Tree = {
    Java.reanalyzeTrees(newUnits, context)
    new Tree(newUnits, context, originTrackedNames)
  }
}