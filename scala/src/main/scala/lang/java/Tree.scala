package lang.java

import java.io.File

import com.sun.source.tree.TreeVisitor
import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit
import com.sun.tools.javac.tree.{TreeMaker, TreeCopier, JCTree}
import com.sun.tools.javac.util.{Context, Log}
import name.{NameGraph, Name, Nominal}

class Tree(val units: List[JCCompilationUnit], context: Context) extends Nominal {

  lazy val (_resolveNames, symMap, nodeMap): (NameGraph, Map[Symbol, Name], Map[JCTree, Name]) = {
    val visitor = new NameGraphExtractor
    for (unit <- units)
      unit.accept(visitor, null)
    (NameGraph(visitor.names, visitor.edges), visitor.symMap, visitor.nodeMap)
  }

  def resolveNames = _resolveNames
  def allNames: Set[Name.ID] = resolveNames.V

  def rename(renaming: Renaming): Nominal = {
    val visitor = new RenameVisitor(renaming, nodeMap, TreeMaker.instance(context)).asInstanceOf[TreeVisitor[Void,Void]]
    var newUnits = List[JCCompilationUnit]()
    for (unit <- units) {
      val newUnit = unit.accept(visitor, null).asInstanceOf[JCCompilationUnit]
      newUnit.sourcefile = unit.sourcefile
      newUnits = newUnits :+ newUnit
    }
    Tree.fromTrees(newUnits, context)
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
}


object Tree {
  def fromSourceFiles(sourceFiles: Seq[File]): Tree = {
    val (units, context) = Java.analyzeSourceFiles(sourceFiles)
    new Tree(units, context)
  }

  def fromSourceCode(sourceFileCodes/* name -> code*/: Map[String, String]): Tree = {
    Tree.fromSourceFiles(Java.makeTemporarySourceFiles(sourceFileCodes))
  }

  def fromTrees(newUnits: List[JCCompilationUnit], context: Context): Tree = {
    Java.reanalyzeTrees(newUnits, context)
    new Tree(newUnits, context)
  }
}