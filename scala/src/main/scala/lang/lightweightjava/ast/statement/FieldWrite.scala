package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.NameGraph._
import name.{Name, NameGraph}

case class FieldWrite(targetObject: TermVariable, targetField: Name, source: TermVariable) extends Statement {
  require(AST.isLegalName(targetField), "Field name '" + targetField + "' is no legal Java field name")

  override def allNames = targetObject.allNames ++ source.allNames + targetField.id

  override def rename(renaming: Renaming) = FieldWrite(targetObject.rename(renaming), renaming(targetField), source.rename(renaming))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    require(targetObject != Null, "Can't access fields of 'null' in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
    typeEnvironment(targetObject) match {
      case className@ClassName(_) => program.findField(program.getClassDefinition(className).get, targetField) match {
        case Some(field) => require(source == Null || program.checkSubclass(typeEnvironment(source), field.fieldType),
          "Field and the variable it is assigned in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "' are incompatible!")
          require(className.className == typeEnvironment(This).className || field.accessModifier == AccessModifier.PUBLIC, "Trying to access private field '" + field.fieldName + "' of class '" + typeEnvironment(targetObject).asInstanceOf[ClassName].className + "' externally!")
          typeEnvironment
        case None =>
          throw new IllegalArgumentException("Class '" + className.className + "' doesn't have field '" + targetField + "' written in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
      }
      case _ => throw new IllegalArgumentException("Class 'Object' doesn't have field '" + targetField + "' written in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) = {
    val variablesGraph = targetObject.resolveVariableNames(methodEnvironment) + source.resolveVariableNames(methodEnvironment)

    // As name resolution doesn't require the program to be type checked, we have to to it here and return an error for unknown fields
    if (typeEnvironment.contains(targetObject) && nameEnvironment.contains(typeEnvironment(targetObject).className)) {
      val fieldMap = nameEnvironment(typeEnvironment(targetObject).className)._2
      if (fieldMap.contains(targetField))
        (variablesGraph + NameGraph(Set(targetField.id), Map(targetField.id -> fieldMap(targetField)), Set()), (methodEnvironment, typeEnvironment))
      else
        (variablesGraph + NameGraph(Set(targetField.id), Map(), Set(UnboundReferenceError(targetField.id))), (methodEnvironment, typeEnvironment))
    }
    else {
      (variablesGraph + NameGraph(Set(targetField.id), Map(), Set(UnboundReferenceError(targetField.id))), (methodEnvironment, typeEnvironment))
    }
  }

  override def toString: String = targetObject.toString + "." + targetField.toString + " = " + source.toString + ";"
}
