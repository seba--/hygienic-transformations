package lang.java

import java.io.File

import com.sun.source.tree.{IdentifierTree, CompilationUnitTree}
import com.sun.source.util.TreeScanner
import com.sun.tools.javac.api.JavacTaskImpl
import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.tree.JCTree.JCIdent
import com.sun.tools.javac.tree.{TreeCopier, JCTree}
import com.sun.tools.javac.util.Log
import name.{NameGraph, Name, Nominal}

class Tree(sourceFiles: Seq[File]) extends Nominal {

  def this(sourceFileCodes/* name -> code*/: Map[String, String]) = {
    this(Java.makeTemporarySourceFiles(sourceFileCodes))
  }

  val (units, context) = Java.parseSourceFiles(sourceFiles)

  val (_resolveNames, symMap, nodeMap): (NameGraph, Map[Symbol, Name], Map[JCTree, Name]) = {
    val visitor = new NameGraphExtractor
    for (unit <- units)
      unit.accept(visitor, null)
    (NameGraph(visitor.names, visitor.edges), visitor.symMap, visitor.nodeMap)
  }

  def resolveNames = _resolveNames
  def allNames: Set[Name.ID] = resolveNames.V

  def rename(renaming: Renaming): Nominal = {
    val visitor = new RenameVisitor(renaming, nodeMap)
    for (unit <- units) {
      unit.accept(visitor, null)
    }
    Java.reanalyzeTrees(units, context)
    this
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

