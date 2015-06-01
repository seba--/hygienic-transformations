package lang.lightweightjava.ast.returnvalue

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.statement.This
import name.Renaming

case class ReturnObjectInstantiation(classRef: ClassRef) extends ReturnValue {
  override def allNames = classRef.allNames

  override def rename(renaming: Renaming) = ReturnObjectInstantiation(classRef.rename(renaming))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment, returnType : ClassRef) = {
    require(program.checkSubclass(classRef, returnType),
      "Object returned by method in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "' is incompatible to the method return type!")

    typeEnvironment
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment: TypeEnvironment) =
    classRef.resolveNames(nameEnvironment)

  override def toString = "new " + classRef.toString + "();"
}