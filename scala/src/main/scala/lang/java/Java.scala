package lang.java

import java.io.{PrintWriter, File}
import java.util
import java.util.Collections
import javax.tools.{JavaFileObject, ToolProvider}

import com.sun.source.tree.CompilationUnitTree
import com.sun.source.util.{JavacTask, Trees}
import com.sun.tools.javac
import com.sun.tools.javac.api.JavacTaskImpl
import com.sun.tools.javac.comp.Check
import com.sun.tools.javac.main.JavaCompiler
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit
import com.sun.tools.javac.util.Context

import scala.collection.JavaConversions

object Java {
  val tmpDir = {
    val f = File.createTempFile("JavaFiles", null)
    f.delete()
    f.mkdir()
    f
  }

  def writeTemporarySourceFile(unitName: String, sourceCode: String) = {
    val sourceFile = new File(tmpDir, s"$unitName.java")
    val printWriter = new PrintWriter(sourceFile)
    try {
      printWriter.write(sourceCode)
    } finally {
      printWriter.close()
    }
    sourceFile
  }

  def makeTemporarySourceFiles(sourceFileCodes /* name -> code*/ : Map[String, String]): Seq[File] = {
    var sourceFiles = Seq[File]()
    for ((name, code) <- sourceFileCodes)
      sourceFiles = sourceFiles :+ writeTemporarySourceFile(name, code)
    sourceFiles
  }

  def analyzeSourceFiles(sourceFiles: Seq[File]): (List[JCCompilationUnit], Context) = {
    val compiler = ToolProvider.getSystemJavaCompiler();
    val fileManager = compiler.getStandardFileManager(null, null, null);
    val compilationUnits = fileManager.getJavaFileObjects(sourceFiles: _*)

    val options = util.Arrays.asList[String]()
    val classes = util.Arrays.asList[String]()
    val compilationTask = compiler.getTask(null, fileManager, null, options, classes, compilationUnits).asInstanceOf[JavacTaskImpl]

    val trees = compilationTask.parse()
    compilationTask.analyze()

    var result = List[JCCompilationUnit]()
    val it = trees.iterator
    while (it.hasNext)
      result = result :+ it.next.asInstanceOf[JCCompilationUnit]
    (result, compilationTask.getContext)
  }

  def reanalyzeTrees(trees: List[JCCompilationUnit], oldContext: Context): Unit = {
    val treeList = javac.util.List.from(trees.toArray)

    Check.instance(oldContext).compiled.clear()
    val compiler = JavaCompiler.instance(oldContext)
    compiler.log
    compiler.enterTrees(treeList)
    compiler.flow(compiler.attribute(compiler.todo))
  }
}