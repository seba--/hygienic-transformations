package lang.java

import java.io.File

import com.sun.tools.javac.code.Symbol
import name.{NameGraph, Name, Nominal}

class Tree(sourceFiles: Seq[File]) extends Nominal {

  def this(sourceFileCodes/* name -> code*/: Map[String, String]) = {
    this(Java.makeTemporarySourceFiles(sourceFileCodes))
  }

  val units = Java.parseSourceFiles(sourceFiles)
  
  private lazy val names: (NameGraph, Map[Symbol, Name]) = {
    val visitor = new NameGraphExtractor
    for (unit <- units)
      unit.accept(visitor, null)
    (NameGraph(visitor.names, visitor.edges), visitor.symMap)
  }

  def resolveNames = names._1
  def symMap = names._2
  
  def allNames: Set[Name.ID] = resolveNames.V
  def rename(renaming: Renaming): Nominal = ???
}

