package lang.java

import java.io.File

import name.{NameGraph, Name, Nominal}

class Code(sourceFiles: Seq[File]) extends Nominal {

  def this(sourceFileCodes/* name -> code*/: Map[String, String]) = {
    this(Java.makeTemporarySourceFiles(sourceFileCodes))
  }

  val unit = Java.parseSourceFiles(sourceFiles)
//  val ast = unit.getAST

  def allNames: Set[Name.ID] = ???
  def rename(renaming: Renaming): Nominal = ???
  def resolveNames: NameGraph = ???

}
