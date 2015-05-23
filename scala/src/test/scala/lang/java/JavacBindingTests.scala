package lang.java

import java.io.{OutputStream, PrintWriter}

import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.main.JavaCompiler
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree._
import com.sun.tools.javac.util.Log
import name.NameGraph
import org.eclipse.jdt.core.dom.{ExpressionStatement, SimpleName, ReturnStatement, TypeDeclaration}
import org.scalatest._

class JavacBindingTests extends FunSuite {

  val nullWriter = new PrintWriter(new OutputStream() {def write(b: Int) {}})

  def assertEdge(tree: Tree, refDec: (JCTree, JCTree), sym: Symbol = null): Unit = {
    val ref = refDec._1
    val dec = refDec._2
    if (sym != null)
      assert(tree.symMap(sym).id == tree.nodeMap(dec).id)
    assert(tree.nodeMap(dec).id != tree.nodeMap(ref).id)
    assert(tree.nodeMap(dec).id == tree.resolveNames.E(tree.nodeMap(ref).id))
  }

  def assertNotEdge(tree: Tree, refDec: (JCTree, JCTree)): Unit = {
    val ref = refDec._1
    val dec = refDec._2
    assert(tree.nodeMap(dec).id != tree.nodeMap(ref).id)
    assert(tree.nodeMap(dec).id != tree.resolveNames.E(tree.nodeMap(ref).id))
  }

  val personCode =
    """
      |import java.util.Date;
      |public class Person {
      |
      |  private String name;
      |  private Date birthDate;
      |
      |  public Person(String name, Date birthDate) {
      |    this.name = name;
      |    this.birthDate = birthDate;
      |  }
      |
      |  public String getName() { return name; }
      |  public Date getBirthDate() { return birthDate; }
      |  @Override
      |  public String toString() {
      |    String n = getName();
      |    Date d = getBirthDate();
      |    return n + " ~ " + d;
      |  }
      |}
    """.stripMargin

  val studentCode =
    """
      |import java.util.Date;
      |public class Student extends Person {
      |
      |  private int studentId;
      |
      |  public Student(String name, Date birthDate, int studentID) {
      |    super(name, birthDate);
      |    this.studentId = studentId;
      |  }
      |
      |  public int getStudentId() { return studentId; }
      |  @Override
      |  public String toString() {
      |    String n = getName();
      |    Date d = getBirthDate();
      |    int id = getStudentId();
      |    return n + " ~ " + d + "@" + id;
      |  }
      |}
    """.stripMargin

  val personTree = new Tree(Map("Person" -> personCode))
  val person = personTree.units.head
  object personStuff {
    val tree = personTree
    val clazz_person = person.getTypeDecls.get(0).asInstanceOf[JCClassDecl]
    val field_name = clazz_person.getMembers.get(0).asInstanceOf[JCVariableDecl]
    val field_birthDate = clazz_person.getMembers.get(1).asInstanceOf[JCVariableDecl]
    val constructor = clazz_person.getMembers.get(2).asInstanceOf[JCMethodDecl]
    
    val method_getName = clazz_person.getMembers.get(3).asInstanceOf[JCMethodDecl]
    private val getName_ret = method_getName.getBody.getStatements.get(0).asInstanceOf[JCReturn]
    val field_name_getterReference = getName_ret.getExpression.asInstanceOf[JCIdent]

    val method_getBirthDate = clazz_person.getMembers.get(4).asInstanceOf[JCMethodDecl]
    private val getBirthDate_ret = method_getBirthDate.getBody.getStatements.get(0).asInstanceOf[JCReturn]
    val field_birthDate_getterReference = getBirthDate_ret.getExpression.asInstanceOf[JCIdent]
  }

  val studentTree = new Tree(Map("Person" -> personCode, "Student" -> studentCode))
  val studentPerson = studentTree.units.head
  val student = studentTree.units.tail.head
  object studentStuff {
    val tree = studentTree
    val clazz_person = studentPerson.getTypeDecls.get(0).asInstanceOf[JCClassDecl]
    val field_name = clazz_person.getMembers.get(0).asInstanceOf[JCVariableDecl]
    val field_birthDate = clazz_person.getMembers.get(1).asInstanceOf[JCVariableDecl]
    val constructor = clazz_person.getMembers.get(2).asInstanceOf[JCMethodDecl]

    val method_getName = clazz_person.getMembers.get(3).asInstanceOf[JCMethodDecl]
    private val getName_ret = method_getName.getBody.getStatements.get(0).asInstanceOf[JCReturn]
    val field_name_getterReference = getName_ret.getExpression.asInstanceOf[JCIdent]

    val method_getBirthDate = clazz_person.getMembers.get(4).asInstanceOf[JCMethodDecl]
    private val getBirthDate_ret = method_getBirthDate.getBody.getStatements.get(0).asInstanceOf[JCReturn]
    val field_birthDate_getterReference = getBirthDate_ret.getExpression.asInstanceOf[JCIdent]

    val clazz_student = student.getTypeDecls.get(0).asInstanceOf[JCClassDecl]
    val extending_student = clazz_student.extending.asInstanceOf[JCIdent]
    val field_studentId = clazz_student.getMembers.get(0).asInstanceOf[JCVariableDecl]

    val method_getStudentId = clazz_student.getMembers.get(2).asInstanceOf[JCMethodDecl]
    private val getStudentId_ret = method_getStudentId.getBody.getStatements.get(0).asInstanceOf[JCReturn]
    val field_studentId_getterReference = getStudentId_ret.getExpression.asInstanceOf[JCIdent]
  }

  test("resolve Person field access") {
    import personStuff._
    assertResult(field_name.sym)(field_name_getterReference.sym)
    assertResult(field_birthDate.sym)(field_birthDate_getterReference.sym)
    assert(field_name.sym != field_birthDate_getterReference.sym)
    assert(field_birthDate.sym != field_name_getterReference.sym)
  }

  test("extract Person name graph") {
    import personStuff._
    assertEdge(tree, field_name_getterReference -> field_name, field_name.sym)
    assertEdge(tree, field_birthDate_getterReference -> field_birthDate, field_birthDate.sym)
    assertNotEdge(tree, field_birthDate_getterReference -> field_name)
    assertNotEdge(tree, field_name_getterReference -> field_birthDate)
  }

  test("resolve Student field access") {
    import studentStuff._
    assertResult(field_name.sym)(field_name_getterReference.sym)
    assertResult(field_birthDate.sym)(field_birthDate_getterReference.sym)
    assert(field_name.sym != field_birthDate_getterReference.sym)
    assert(field_birthDate.sym != field_name_getterReference.sym)

    assertResult(clazz_person.sym)(extending_student.sym)
    assertResult(field_studentId.sym)(field_studentId_getterReference.sym)
    assert(field_studentId.sym != field_birthDate_getterReference.sym)
    assert(field_studentId.sym != field_name_getterReference.sym)

  }

  test("extract Student name graph") {
    import studentStuff._
    assertEdge(tree, field_name_getterReference -> field_name, field_name.sym)
    assertEdge(tree, field_birthDate_getterReference -> field_birthDate, field_birthDate.sym)
    assertNotEdge(tree, field_birthDate_getterReference -> field_name)
    assertNotEdge(tree, field_name_getterReference -> field_birthDate)

    assertEdge(tree, extending_student -> clazz_person, clazz_person.sym)
    assertEdge(tree, field_studentId_getterReference -> field_studentId, field_studentId.sym)
    assertNotEdge(tree, field_birthDate_getterReference -> field_studentId)
    assertNotEdge(tree, field_name_getterReference -> field_studentId)
  }

  test("inconsistent renaming field birthDate->birthDate2 in Person") {
    import personStuff._
    val renaming = Map(tree.nodeMap(field_birthDate).id -> "birthDate2")

    tree.silent {
      tree.rename(renaming)
    }

    assert(field_birthDate.sym != field_birthDate_getterReference.sym)
    assert(field_name.sym != field_birthDate_getterReference.sym)
    assert(field_birthDate.sym != field_name_getterReference.sym)
  }

  test("consistent renaming field birthDate->birthDate2 in Person") {
    import personStuff._
    val constructorRef = constructor.getBody.stats.get(2).asInstanceOf[JCExpressionStatement].expr.asInstanceOf[JCAssign].lhs

    val renaming = Map(
      tree.nodeMap(field_birthDate).id -> "birthDate2",
      tree.nodeMap(constructorRef).id -> "birthDate2",
      tree.nodeMap(field_birthDate_getterReference).id -> "birthDate2"
    )

    tree.rename(renaming)

    assertResult(field_name.sym)(field_name_getterReference.sym)
    assertResult(field_birthDate.sym)(field_birthDate_getterReference.sym)
    assert(field_name.sym != field_birthDate_getterReference.sym)
    assert(field_birthDate.sym != field_name_getterReference.sym)
  }

}