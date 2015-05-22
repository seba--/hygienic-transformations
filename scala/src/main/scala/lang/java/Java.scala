package lang.java

import java.io.{PrintWriter, File}
import java.util
import javax.tools.JavaCompiler.CompilationTask
import javax.tools.{ToolProvider, JavaCompiler}

import com.sun.source.tree.CompilationUnitTree
import com.sun.source.util.{JavacTask, Trees}
import org.eclipse.core.resources.{IResource, IProject}
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.{CompilationUnit, AST, ASTParser}
import org.eclipse.jdt.internal.core.{JavaModelManager, JavaProject}

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

  def makeTemporarySourceFiles(sourceFileCodes/* name -> code*/: Map[String, String]): Seq[File] = {
    var sourceFiles = Seq[File]()
    for ((name, code) <- sourceFileCodes)
      sourceFiles = sourceFiles :+ writeTemporarySourceFile(name, code)
    sourceFiles
  }

  def parseSourceFiles(sourceFiles: Seq[File]): List[CompilationUnitTree] = {
    val compiler = ToolProvider.getSystemJavaCompiler();
    val fileManager = compiler.getStandardFileManager(null, null, null);
    val compilationUnits = fileManager.getJavaFileObjects(sourceFiles:_*)
    val unit = compilationUnits.iterator().next()

    val options = util.Arrays.asList[String]()
    val classes = util.Arrays.asList[String]()
    val compilationTask = compiler.getTask(null, fileManager, null, options, classes, compilationUnits).asInstanceOf[JavacTask]

    val trees = compilationTask.parse()
    compilationTask.analyze()

    var result = List[CompilationUnitTree]()
    val it = trees.iterator
    while (it.hasNext)
      result = result :+ it.next
    result
  }

//  def newProject = JavaCore.create(DummyIProject).asInstanceOf[JavaProject]

//  def parseSourceFile(unitName: String, sourceCode: String, javaProject: JavaProject = newProject): CompilationUnit = {
//    val parser = ASTParser.newParser(AST.JLS3)
//    parser.setSource(sourceCode.toCharArray)
//    parser.setResolveBindings(true)
//    parser.setProject(javaProject)
//    parser.setUnitName(unitName)
//    parser.createAST(null).asInstanceOf[CompilationUnit]
//  }
}


//object DummyIProject extends IProject {
//  new JavaCore()
//  JavaModelManager.getJavaModelManager.startup()
//
//  // Members declared in org.eclipse.core.runtime.IAdaptable
//  def getAdapter(x$1: Class[_]): Object = ???
//
//  // Members declared in org.eclipse.core.resources.IContainer
//  def exists(x$1: org.eclipse.core.runtime.IPath): Boolean = ???
//  def findDeletedMembersWithHistory(x$1: Int,x$2: org.eclipse.core.runtime.IProgressMonitor): Array[org.eclipse.core.resources.IFile] = ???
//  def findMember(x$1: org.eclipse.core.runtime.IPath,x$2: Boolean): org.eclipse.core.resources.IResource = ???
//  def findMember(x$1: org.eclipse.core.runtime.IPath): org.eclipse.core.resources.IResource = ???
//  def findMember(x$1: String,x$2: Boolean): org.eclipse.core.resources.IResource = ???
//  def findMember(x$1: String): org.eclipse.core.resources.IResource = ???
//  def getDefaultCharset(x$1: Boolean): String = ???
//  def getDefaultCharset(): String = "UTF-8"
//  def getFile(x$1: org.eclipse.core.runtime.IPath): org.eclipse.core.resources.IFile = ???
//  def getFolder(x$1: org.eclipse.core.runtime.IPath): org.eclipse.core.resources.IFolder = ???
//  def members(x$1: Int): Array[org.eclipse.core.resources.IResource] = ???
//  def members(x$1: Boolean): Array[org.eclipse.core.resources.IResource] = ???
//  def members(): Array[org.eclipse.core.resources.IResource] = ???
//  def setDefaultCharset(x$1: String,x$2: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def setDefaultCharset(x$1: String): Unit = ???
//
//  // Members declared in org.eclipse.core.resources.IProject
//  def build(x$1: Int,x$2: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def build(x$1: Int,x$2: String,x$3: java.util.Map[_, _],x$4: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def close(x$1: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def create(x$1: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def create(x$1: org.eclipse.core.resources.IProjectDescription,x$2: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def delete(x$1: Boolean,x$2: Boolean,x$3: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def getContentTypeMatcher(): org.eclipse.core.runtime.content.IContentTypeMatcher = ???
//  def getDescription(): org.eclipse.core.resources.IProjectDescription = ???
//  def getFile(x$1: String): org.eclipse.core.resources.IFile = ???
//  def getFolder(x$1: String): org.eclipse.core.resources.IFolder = ???
//  def getNature(x$1: String): org.eclipse.core.resources.IProjectNature = ???
//  def getPluginWorkingLocation(x$1: org.eclipse.core.runtime.IPluginDescriptor): org.eclipse.core.runtime.IPath = ???
//  def getReferencedProjects(): Array[org.eclipse.core.resources.IProject] = ???
//  def getReferencingProjects(): Array[org.eclipse.core.resources.IProject] = ???
//  def getWorkingLocation(x$1: String): org.eclipse.core.runtime.IPath = ???
//  def hasNature(x$1: String): Boolean = false
//  def isNatureEnabled(x$1: String): Boolean = ???
//  def isOpen(): Boolean = ???
//  def move(x$1: org.eclipse.core.resources.IProjectDescription,x$2: Boolean,x$3: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def open(x$1: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def open(x$1: Int,x$2: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def setDescription(x$1: org.eclipse.core.resources.IProjectDescription,x$2: Int,x$3: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def setDescription(x$1: org.eclipse.core.resources.IProjectDescription,x$2: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//
//  // Members declared in org.eclipse.core.resources.IResource
//  def accept(x$1: org.eclipse.core.resources.IResourceVisitor,x$2: Int,x$3: Int): Unit = ???
//  def accept(x$1: org.eclipse.core.resources.IResourceVisitor,x$2: Int,x$3: Boolean): Unit = ???
//  def accept(x$1: org.eclipse.core.resources.IResourceVisitor): Unit = ???
//  def accept(x$1: org.eclipse.core.resources.IResourceProxyVisitor,x$2: Int): Unit = ???
//  def clearHistory(x$1: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def copy(x$1: org.eclipse.core.resources.IProjectDescription,x$2: Int,x$3: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def copy(x$1: org.eclipse.core.resources.IProjectDescription,x$2: Boolean,x$3: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def copy(x$1: org.eclipse.core.runtime.IPath,x$2: Int,x$3: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def copy(x$1: org.eclipse.core.runtime.IPath,x$2: Boolean,x$3: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def createMarker(x$1: String): org.eclipse.core.resources.IMarker = ???
//  def createProxy(): org.eclipse.core.resources.IResourceProxy = ???
//  def delete(x$1: Int,x$2: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def delete(x$1: Boolean,x$2: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def deleteMarkers(x$1: String,x$2: Boolean,x$3: Int): Unit = ???
//  def exists(): Boolean = ???
//  def findMarker(x$1: Long): org.eclipse.core.resources.IMarker = ???
//  def findMarkers(x$1: String,x$2: Boolean,x$3: Int): Array[org.eclipse.core.resources.IMarker] = ???
//  def findMaxProblemSeverity(x$1: String,x$2: Boolean,x$3: Int): Int = ???
//  def getFileExtension(): String = ???
//  def getFullPath(): org.eclipse.core.runtime.IPath = ???
//  def getLocalTimeStamp(): Long = ???
//  def getLocation(): org.eclipse.core.runtime.IPath = ???
//  def getLocationURI(): java.net.URI = ???
//  def getMarker(x$1: Long): org.eclipse.core.resources.IMarker = ???
//  def getModificationStamp(): Long = ???
//  def getName(): String = "DummyIProject"
//  def getParent(): org.eclipse.core.resources.IContainer = ???
//  def getPersistentProperty(x$1: org.eclipse.core.runtime.QualifiedName): String = ???
//  def getProject(): org.eclipse.core.resources.IProject = ???
//  def getProjectRelativePath(): org.eclipse.core.runtime.IPath = ???
//  def getRawLocation(): org.eclipse.core.runtime.IPath = ???
//  def getRawLocationURI(): java.net.URI = ???
//  def getResourceAttributes(): org.eclipse.core.resources.ResourceAttributes = ???
//  def getSessionProperty(x$1: org.eclipse.core.runtime.QualifiedName): Object = ???
//  def getType(): Int = IResource.PROJECT
//  def getWorkspace(): org.eclipse.core.resources.IWorkspace = ???
//  def isAccessible(): Boolean = ???
//  def isDerived(): Boolean = ???
//  def isLinked(x$1: Int): Boolean = ???
//  def isLinked(): Boolean = ???
//  def isLocal(x$1: Int): Boolean = ???
//  def isPhantom(): Boolean = ???
//  def isReadOnly(): Boolean = ???
//  def isSynchronized(x$1: Int): Boolean = ???
//  def isTeamPrivateMember(): Boolean = ???
//  def move(x$1: org.eclipse.core.resources.IProjectDescription,x$2: Int,x$3: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def move(x$1: org.eclipse.core.resources.IProjectDescription,x$2: Boolean,x$3: Boolean,x$4: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def move(x$1: org.eclipse.core.runtime.IPath,x$2: Int,x$3: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def move(x$1: org.eclipse.core.runtime.IPath,x$2: Boolean,x$3: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def refreshLocal(x$1: Int,x$2: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def revertModificationStamp(x$1: Long): Unit = ???
//  def setDerived(x$1: Boolean): Unit = ???
//  def setLocal(x$1: Boolean,x$2: Int,x$3: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//  def setLocalTimeStamp(x$1: Long): Long = ???
//  def setPersistentProperty(x$1: org.eclipse.core.runtime.QualifiedName,x$2: String): Unit = ???
//  def setReadOnly(x$1: Boolean): Unit = ???
//  def setResourceAttributes(x$1: org.eclipse.core.resources.ResourceAttributes): Unit = ???
//  def setSessionProperty(x$1: org.eclipse.core.runtime.QualifiedName,x$2: Any): Unit = ???
//  def setTeamPrivateMember(x$1: Boolean): Unit = ???
//  def touch(x$1: org.eclipse.core.runtime.IProgressMonitor): Unit = ???
//
//  // Members declared in org.eclipse.core.runtime.jobs.ISchedulingRule
//  def contains(x$1: org.eclipse.core.runtime.jobs.ISchedulingRule): Boolean = ???
//  def isConflicting(x$1: org.eclipse.core.runtime.jobs.ISchedulingRule): Boolean = ???
//}
