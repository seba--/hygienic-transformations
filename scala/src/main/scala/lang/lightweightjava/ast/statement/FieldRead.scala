package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.{Name, NameGraph}
import name.NameGraph._

case class FieldRead(target: VariableName, sourceObject: TermVariable, sourceField: Name) extends Statement {
  require(AST.isLegalName(sourceField), "Field name '" + sourceField + "' is no legal Java field name")

  override def allNames = sourceObject.allNames ++ target.allNames + sourceField.id

  override def rename(renaming: Renaming) = FieldRead(target.rename(renaming), sourceObject.rename(renaming), renaming(sourceField))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    require(sourceObject != Null, "Can't access fields of 'null' in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
    typeEnvironment(sourceObject) match {
      case className@ClassName(_) => program.findField(program.getClassDefinition(className).get, sourceField) match {
        case Some(field) => require(program.checkSubclass(field.fieldType, typeEnvironment(target)),
          "Variable and the field it is assigned in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "' are incompatible!")
          require(className.className == typeEnvironment(This).className || field.accessModifier == AccessModifier.PUBLIC, "Trying to access private field '" + field.fieldName + "' of class '" + typeEnvironment(sourceObject).asInstanceOf[ClassName].className + "' externally!")
          typeEnvironment
        case None =>
          throw new IllegalArgumentException("Class '" + className.className + "' doesn't have field '" + sourceField + "' read in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
      }
      case _ => throw new IllegalArgumentException("Class 'Object' doesn't have field '" + sourceField + "' read in class '" + typeEnvironment(This).asInstanceOf[ClassName].className + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) = {
    val variablesGraph = target.resolveVariableNames(methodEnvironment) + sourceObject.resolveVariableNames(methodEnvironment)

    // As name resolution doesn't require the program to be type checked, we have to to it here and return an error for unknown fields
    if (typeEnvironment.contains(sourceObject) && nameEnvironment.contains(typeEnvironment(sourceObject).className)) {
      val fieldMap = nameEnvironment(typeEnvironment(sourceObject).className)._2
      if (fieldMap.contains(sourceField))
        (variablesGraph + NameGraph(Set(sourceField.id), Map(sourceField.id -> fieldMap(sourceField)), Set()), (methodEnvironment, typeEnvironment))
      else
        (variablesGraph + NameGraph(Set(sourceField.id), Map(), Set()), (methodEnvironment, typeEnvironment))
    }
    else {
      (variablesGraph + NameGraph(Set(sourceField.id), Map(), Set()), (methodEnvironment, typeEnvironment))
    }
  }

  override def toString = target.toString + " = " + sourceObject.toString + "." + sourceField.toString + ";"
}
