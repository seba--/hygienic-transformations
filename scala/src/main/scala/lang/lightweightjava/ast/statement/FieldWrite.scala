package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.namegraph.NameGraph
import name.{Identifier, Renaming}

case class FieldWrite(targetObject: TermVariable, targetField: Identifier, source: TermVariable) extends Statement {
  require(AST.isLegalName(targetField.name), "Field name '" + targetField + "' is no legal Java field name")

  override def allNames = targetObject.allNames ++ source.allNames + targetField

  override def rename(renaming: Renaming) = FieldWrite(targetObject.rename(renaming), renaming(targetField), source.rename(renaming))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    require(targetObject != Null, "Can't access fields of 'null' in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
    typeEnvironment(targetObject) match {
      case className@ClassName(_) => program.findField(program.getClassDefinition(className).get, targetField.name) match {
        case Some(field) => require(source == Null || program.checkSubclass(typeEnvironment(source), field.fieldType),
          "Field and the variable it is assigned in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "' are incompatible!")
          require(className.name == typeEnvironment(This).name || field.accessModifier == AccessModifier.PUBLIC,
            "Trying to access private field '" + field.fieldName + "' of class '" + typeEnvironment(targetObject).asInstanceOf[ClassName].name + "' externally!")
          typeEnvironment
        case None =>
          throw new IllegalArgumentException("Class '" + className.name + "' doesn't have field '" + targetField + "' written in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
      }
      case _ => throw new IllegalArgumentException("Class 'Object' doesn't have field '" + targetField + "' written in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) = {
    val variablesGraph = targetObject.resolveVariableNames(methodEnvironment) + source.resolveVariableNames(methodEnvironment)

    // As name resolution doesn't require the program to be type checked, we have to to it here and return an error for unknown fields
    if (typeEnvironment.contains(targetObject) && nameEnvironment.contains(typeEnvironment(targetObject).name)) {
      val fieldMap = nameEnvironment(typeEnvironment(targetObject).name)._2
      if (fieldMap.contains(targetField.name))
        (variablesGraph + NameGraph(Set(targetField), Map(targetField -> fieldMap(targetField.name))), (methodEnvironment, typeEnvironment))
      else
        (variablesGraph + NameGraph(Set(targetField), Map()), (methodEnvironment, typeEnvironment))
    }
    else {
      (variablesGraph + NameGraph(Set(targetField), Map()), (methodEnvironment, typeEnvironment))
    }
  }

  override def toString: String = targetObject.toString + "." + targetField.toString + " = " + source.toString + ";"
}
