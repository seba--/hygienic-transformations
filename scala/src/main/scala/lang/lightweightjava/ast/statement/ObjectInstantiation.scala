package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.Renaming

case class ObjectInstantiation(target: VariableName, classRef: ClassRef) extends Statement {
  override def allNames = target.allNames ++ classRef.allNames

  override def rename(renaming: Renaming) = ObjectInstantiation(target.rename(renaming).asInstanceOf[VariableName], classRef.rename(renaming))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    require(program.checkSubclass(classRef, typeEnvironment(target)),
      "Variable and the object it is assigned in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "' are incompatible!")
    typeEnvironment
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) =
    (target.resolveVariableNames(methodEnvironment) + classRef.resolveNames(nameEnvironment), (methodEnvironment, typeEnvironment))

  override def toString: String = target.toString + " = new " + classRef.toString + "();"
}
