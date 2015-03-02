package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.namegraph.NameGraph
import name.{Identifier, Renaming}

case class FieldRead(target: VariableName, sourceObject: TermVariable, sourceField: Identifier) extends Statement {
  require(AST.isLegalName(sourceField.name), "Field name '" + sourceField + "' is no legal Java field name")

  override def allNames = sourceObject.allNames ++ target.allNames + sourceField

  override def rename(renaming: Renaming) = FieldRead(target.rename(renaming).asInstanceOf[VariableName], sourceObject.rename(renaming), renaming(sourceField))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    require(sourceObject != Null, "Can't access fields of 'null' in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
    typeEnvironment(sourceObject) match {
      case className@ClassName(_) => program.findField(program.getClassDefinition(className).get, sourceField.name) match {
        case Some(field) => require(program.checkSubclass(field.fieldType, typeEnvironment(target)),
          "Variable and the field it is assigned in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "' are incompatible!")
          require(className.name == typeEnvironment(This).name || field.accessModifier == AccessModifier.PUBLIC,
            "Trying to access private field '" + field.fieldName + "' of class '" + typeEnvironment(sourceObject).asInstanceOf[ClassName].name + "' externally!")
          typeEnvironment
        case None =>
          throw new IllegalArgumentException("Class '" + className.name + "' doesn't have field '" + sourceField + "' read in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
      }
      case _ => throw new IllegalArgumentException("Class 'Object' doesn't have field '" + sourceField + "' read in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) = {
    val variablesGraph = target.resolveVariableNames(methodEnvironment) + sourceObject.resolveVariableNames(methodEnvironment)

    // As name resolution doesn't require the program to be type checked, we have to to it here and return an error for unknown fields
    if (typeEnvironment.contains(sourceObject) && nameEnvironment.contains(typeEnvironment(sourceObject).name)) {
      val fieldMap = nameEnvironment(typeEnvironment(sourceObject).name)._2
      if (fieldMap.contains(sourceField.name))
        (variablesGraph + NameGraph(Set(sourceField), Map(sourceField -> fieldMap(sourceField.name))), (methodEnvironment, typeEnvironment))
      else
        (variablesGraph + NameGraph(Set(sourceField), Map()), (methodEnvironment, typeEnvironment))
    }
    else {
      (variablesGraph + NameGraph(Set(sourceField), Map()), (methodEnvironment, typeEnvironment))
    }
  }

  override def toString = target.toString + " = " + sourceObject.toString + "." + sourceField.toString + ";"
}
