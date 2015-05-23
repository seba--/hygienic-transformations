package lang.java

import java.io.File

import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.tree.JCTree
import name.{NameGraph, Name, Nominal}

class Tree(sourceFiles: Seq[File]) extends Nominal {

  def this(sourceFileCodes/* name -> code*/: Map[String, String]) = {
    this(Java.makeTemporarySourceFiles(sourceFileCodes))
  }

  val units = Java.parseSourceFiles(sourceFiles)

  val (_resolveNames, symMap, nodeMap): (NameGraph, Map[Symbol, Name], Map[JCTree, Name]) = {
    val visitor = new NameGraphExtractor
    for (unit <- units)
      unit.accept(visitor, null)
    (NameGraph(visitor.names, visitor.edges), visitor.symMap, visitor.nodeMap)
  }

  def resolveNames = _resolveNames
  def allNames: Set[Name.ID] = resolveNames.V
  def rename(renaming: Renaming): Nominal = ???
}

