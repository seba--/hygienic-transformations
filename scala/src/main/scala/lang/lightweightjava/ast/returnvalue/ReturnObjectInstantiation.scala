package lang.lightweightjava.ast.returnvalue

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.statement.This
import name.NameGraph

case class ReturnObjectInstantiation(classRef: ClassRef) extends ReturnValue {
  override def allNames = classRef.allNames

  override def rename(renaming: Renaming) = ReturnObjectInstantiation(classRef.rename(renaming))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment, returnType : ClassRef) = {
    require(program.checkSubclass(classRef, returnType),
      "Object returned by method in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "' is incompatible to the method return type!")
    typeEnvironment
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment: TypeEnvironment): NameGraph =
    classRef.resolveNames(nameEnvironment)

  override def toString: String = "new " + classRef.toString + "();"
}