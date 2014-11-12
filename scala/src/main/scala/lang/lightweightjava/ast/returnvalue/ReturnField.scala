package lang.lightweightjava.ast.returnvalue

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.statement.{Null, TermVariable, This}
import name.{Name, NameGraph}

case class ReturnField(returnObject: TermVariable, returnField: Name) extends ReturnValue {
  require(AST.isLegalName(returnField), "Field name '" + returnField + "' is no legal Java field name")

  override def allNames = returnObject.allNames + returnField.id

  override def rename(renaming: Renaming) = ReturnField(returnObject.rename(renaming), renaming(returnField))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment, returnType : ClassRef) = {
    require(returnObject != Null, "Can't access fields of 'null' in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
    typeEnvironment(returnObject) match {
      case className@ClassName(_) => program.findField(program.getClassDefinition(className).get, returnField) match {
        case Some(field) => require(program.checkSubclass(field.fieldType, returnType),
          "Field returned by a method in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "' is incompatible with the method return type!")
          require(className.className == typeEnvironment(This).className || field.accessModifier == AccessModifier.PUBLIC, "Trying to access private field '" + field.fieldName + "' of class '" + typeEnvironment(returnObject).asInstanceOf[ClassName].className + "' externally!")
          typeEnvironment
        case None =>
          throw new IllegalArgumentException("Class '" + className.className + "' doesn't have field '" + returnField + "' returned in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
      }
      case _ => throw new IllegalArgumentException("Class 'Object' doesn't have field '" + returnField + "' returned in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment): NameGraph = {
    val variablesGraph = returnObject.resolveVariableNames(methodEnvironment)

    // As name resolution doesn't require the program to be type checked, we have to to it here and return an error for unknown fields
    if (typeEnvironment.contains(returnObject) && nameEnvironment.contains(typeEnvironment(returnObject).className)) {
      val fieldMap = nameEnvironment(typeEnvironment(returnObject).className)._2
      if (fieldMap.contains(returnField))
        variablesGraph + NameGraph(Set((returnField.id, false)), Map(returnField.id -> fieldMap(returnField)), Set())
      else
        variablesGraph + NameGraph(Set((returnField.id, false)), Map(), Set())
    }
    else {
      variablesGraph + NameGraph(Set((returnField.id, false)), Map(), Set())
    }
  }

  override def toString: String = returnObject.toString + "." + returnField.toString
}