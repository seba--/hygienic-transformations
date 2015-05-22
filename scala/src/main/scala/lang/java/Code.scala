package lang.java

import java.io.File

import name.{NameGraph, Name, Nominal}

class Code(sourceFiles: Seq[File]) extends Nominal {

  def this(sourceFileCodes/* name -> code*/: Map[String, String]) = {
    this(Java.makeTemporarySourceFiles(sourceFileCodes))
  }

  val units = Java.parseSourceFiles(sourceFiles)
  
  def allNames: Set[Name.ID] = ???
  def rename(renaming: Renaming): Nominal = ???
  
  lazy val resolveNames: NameGraph = {
    ???
  }

}
