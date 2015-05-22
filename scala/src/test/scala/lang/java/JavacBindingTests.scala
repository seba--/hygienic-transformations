package lang.java

import com.sun.tools.javac.tree.JCTree._
import org.eclipse.jdt.core.dom.{ExpressionStatement, SimpleName, ReturnStatement, TypeDeclaration}
import org.scalatest._

class JavacBindingTests extends FunSuite {


  def loadSourceCode(unitName: String, sourceCode: String): Code = new Code(Map(unitName -> sourceCode))

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
      |}
    """.stripMargin

  val person = loadSourceCode("Person", personCode).unit.head
  object personStuff {
    val clazz = person.getTypeDecls.get(0).asInstanceOf[JCClassDecl]
    val field_name = clazz.getMembers.get(0).asInstanceOf[JCVariableDecl]
    val field_birthDate = clazz.getMembers.get(1).asInstanceOf[JCVariableDecl]
    val constructor = clazz.getMembers.get(2).asInstanceOf[JCMethodDecl]
    
    val method_getName = clazz.getMembers.get(3).asInstanceOf[JCMethodDecl]
    private val getName_ret = method_getName.getBody.getStatements.get(0).asInstanceOf[JCReturn]
    val field_name_getterReference = getName_ret.getExpression.asInstanceOf[JCIdent]

    val method_getBirthDate = clazz.getMembers.get(4).asInstanceOf[JCMethodDecl]
    private val getBirthDate_ret = method_getBirthDate.getBody.getStatements.get(0).asInstanceOf[JCReturn]
    val field_birthDate_getterReference = getBirthDate_ret.getExpression.asInstanceOf[JCIdent]
  }

  test("resolve field access") {
    import personStuff._
    assertResult(field_name.sym)(field_name_getterReference.sym)
    assertResult(field_birthDate.sym)(field_birthDate_getterReference.sym)
    assert(field_name.sym != field_birthDate_getterReference.sym)
    assert(field_birthDate.sym != field_name_getterReference.sym)
  }
}