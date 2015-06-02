package lang.java

import java.io.{PrintWriter, File}
import java.util
import java.util.Collections
import javax.tools.{JavaFileObject, ToolProvider}

import com.sun.source.tree.CompilationUnitTree
import com.sun.source.util.{JavacTask, Trees}
import com.sun.tools.javac
import com.sun.tools.javac.api.JavacTaskImpl
import com.sun.tools.javac.code.Symbol.{TypeSymbol, ClassSymbol}
import com.sun.tools.javac.code.Symtab
import com.sun.tools.javac.comp.Check
import com.sun.tools.javac.jvm.ClassReader
import com.sun.tools.javac.main.JavaCompiler
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit
import com.sun.tools.javac.util.{Name, Context}

import scala.collection.JavaConversions

object Java {
  val tmpDir = {
    val f = File.createTempFile("JavaFiles", null)
    f.delete()
    f.mkdir()
    f
  }

  def binDir() = {
    val f = File.createTempFile("JavaBin", null)
    f.delete()
    f.mkdir()
    f
  }

  def binDir(hash: Long) = new File(tmpDir.getParentFile, "JavaBin_" + hash)

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

  def parseSourceFiles(sourceFiles: Seq[File]): (List[JCCompilationUnit], Context) = {
    val compiler = ToolProvider.getSystemJavaCompiler()
    val fileManager = compiler.getStandardFileManager(null, null, null)
    val compilationUnits = fileManager.getJavaFileObjects(sourceFiles: _*)

    val options = util.Arrays.asList[String]()
    val classes = util.Arrays.asList[String]()
    val compilationTask = compiler.getTask(null, fileManager, null, options, classes, compilationUnits).asInstanceOf[JavacTaskImpl]

    val trees = compilationTask.parse()

    var result = List[JCCompilationUnit]()
    val it = trees.iterator
    while (it.hasNext)
      result = result :+ it.next.asInstanceOf[JCCompilationUnit]
    (result, compilationTask.getContext)
  }

  def analyzeTrees(trees: List[JCCompilationUnit], oldContext: Context, classes: Map[Name, ClassSymbol] = Map()): Unit = {
    val treeList = javac.util.List.from(trees.toArray)

    val symTab = Symtab.instance(oldContext)
    for ((name,sym) <- classes)
      symTab.classes.put(TypeSymbol.formFlatName(name, sym.owner), sym)

    Check.instance(oldContext).compiled.clear()
    val compiler = JavaCompiler.instance(oldContext)
    compiler.enterTrees(treeList)
    compiler.flow(compiler.attribute(compiler.todo))
  }
}