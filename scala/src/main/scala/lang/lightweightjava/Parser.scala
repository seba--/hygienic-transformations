package lang.lightweightjava

import lang.lightweightjava.ast.AccessModifier._
import lang.lightweightjava.ast._
import lang.lightweightjava.ast.returnvalue._
import lang.lightweightjava.ast.statement._
import lang.lightweightjava.configuration._
import lang.lightweightjava.localdeclaration.ast.LocalVariableDeclaration
import lang.lightweightjava.whileloops.ast.WhileLoop
import name.Identifier

import scala.util.parsing.combinator.JavaTokenParsers

object Parser extends JavaTokenParsers {
  override def ident = super.ident.filter(AST.isLegalName)

  def configuration:Parser[Configuration] = program ~ statement.* ^^
    (i => NormalConfiguration(i._1, State(), Heap(), i._2:_*))

  def program:Parser[Program] = classDef.* ^^
    (Program(_:_*))

  def classDef:Parser[ClassDefinition] = ("class" ~> ident) ~ ("extends" ~> classRef).? ~ ("{" ~> classElement.* <~ "}") ^^
    (i => ClassDefinition(ClassName(i._1._1), i._1._2.getOrElse(ObjectClass), i._2:_*))

  def classRef:Parser[ClassRef] = "Object" ^^^ ObjectClass | ident ^^
    (i => ClassName(i))

  def accessModifier:Parser[AccessModifier] = "private" ^^^ PRIVATE | "public".? ^^^ PUBLIC

  def classElement:Parser[ClassElement] = fieldDeclaration | methodDefinition

  def fieldDeclaration:Parser[FieldDeclaration] = accessModifier ~ classRef ~ ident <~ ";" ^^
    (i => FieldDeclaration(i._1._1, i._1._2, Identifier(i._2)))

  def methodDefinition:Parser[MethodDefinition] = methodSignature ~ ("{" ~> methodBody <~ "}") ^^
    (i => MethodDefinition(i._1, i._2))

  def methodSignature:Parser[MethodSignature] = accessModifier ~ classRef ~ (ident ~ ("(" ~> variableDeclarationList.? <~ ")")) ^^
    (i => MethodSignature(i._1._1, i._1._2, Identifier(i._2._1), i._2._2.getOrElse(Seq()):_*))

  def variableDeclarationList:Parser[Seq[VariableDeclaration]] = variableDeclaration ~ ("," ~> variableDeclarationList).? ^^
    (i => if (i._2.isDefined) i._1 +: i._2.get else Seq(i._1))

  def variableDeclaration:Parser[VariableDeclaration] = classRef ~ ident ^^
    (i => VariableDeclaration(i._1, VariableName(i._2)))

  def methodBody:Parser[MethodBody] = statement.* ~ ("return" ~> returnValue) ^^
    (i => MethodBody(i._2, i._1:_*))

  def returnValue:Parser[ReturnValue] = termVariable <~ ";" ^^ (i => ReturnVariable(i)) | returnField | returnMethodCall | returnObjectInstantiation

  def returnField:Parser[ReturnField] = termVariable ~ ("." ~> ident) <~ ";" ^^
    (i => ReturnField(i._1, Identifier(i._2)))

  def returnObjectInstantiation:Parser[ReturnObjectInstantiation] = "new" ~> classRef <~ "()" ~ ";" ^^
  (i => ReturnObjectInstantiation(i))

  def returnMethodCall:Parser[ReturnMethodCall] = termVariable ~ (("." ~> ident) ~ ("(" ~> termVariableList.? <~ ")")) <~ ";" ^^
    (i => ReturnMethodCall(i._1, Identifier(i._2._1), i._2._2.getOrElse(Seq()):_*))

  def termVariable:Parser[TermVariable] = "this" ^^^ This | "null" ^^^ Null | ident ^^
    (i => VariableName(i))

  def termVariableList:Parser[Seq[TermVariable]] = termVariable ~ ("," ~> termVariableList).? ^^
    (i => if (i._2.isDefined) i._1 +: i._2.get else Seq(i._1))

  def statement:Parser[Statement] = conditionalBranch | fieldRead | fieldWrite | voidMethodCall | methodCall | objectInstantiation | statementBlock | variableAssignment | localVariableDeclaration | whileLoop

  def conditionalBranch:Parser[ConditionalBranch] = ("if" ~ "(" ~> (termVariable ~ ("==" ~> termVariable) <~ ")")) ~ (statement ~ ("else" ~> statement)) ^^
    (i => ConditionalBranch(i._1._1, i._1._2, i._2._1, i._2._2))

  def fieldRead:Parser[FieldRead] = ident ~ ("=" ~> termVariable) ~ ("." ~> ident) <~ ";" ^^
    (i => FieldRead(VariableName(i._1._1), i._1._2, Identifier(i._2)))

  def fieldWrite:Parser[FieldWrite] = termVariable ~ ("." ~> ident) ~ ("=" ~> termVariable) <~ ";" ^^
    (i => FieldWrite(i._1._1, i._1._2, i._2))

  def voidMethodCall:Parser[VoidMethodCall] = termVariable ~ (("." ~> ident) ~ ("(" ~> termVariableList.? <~ ")")) <~ ";" ^^
    (i => VoidMethodCall(i._1, Identifier(i._2._1), i._2._2.getOrElse(Seq()):_*))

  def methodCall:Parser[MethodCall] = ident ~ ("=" ~> termVariable) ~ (("." ~> ident) ~ ("(" ~> termVariableList.? <~ ")")) <~ ";" ^^
    (i => MethodCall(VariableName(i._1._1), i._1._2, Identifier(i._2._1), i._2._2.getOrElse(Seq()):_*))

  def objectInstantiation:Parser[ObjectInstantiation] = ident ~ ("=" ~ "new" ~> classRef <~ "()" ~ ";") ^^
    (i => ObjectInstantiation(VariableName(i._1), i._2))

  def statementBlock:Parser[StatementBlock] = "{" ~> statement.* <~ "}" ^^
    (i => StatementBlock(i:_*))

  def variableAssignment:Parser[VariableAssignment] = ident ~ ("=" ~> termVariable) <~ ";" ^^
    (i => VariableAssignment(VariableName(i._1), i._2))

  def localVariableDeclaration:Parser[LocalVariableDeclaration] = classRef ~ ident <~ ";" ^^
    (i => LocalVariableDeclaration(i._1, VariableName(i._2)))

  def whileLoop:Parser[WhileLoop] = ("while" ~ "(" ~> (termVariable ~ ("!=" ~> termVariable) <~ ")")) ~ statement ^^
    (i => WhileLoop(i._1._1, i._1._2, i._2))
}
