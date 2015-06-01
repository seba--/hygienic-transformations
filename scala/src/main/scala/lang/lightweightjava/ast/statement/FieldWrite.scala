package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.namegraph.NameGraphExtended
import name.{Identifier, Renaming}

case class FieldWrite(targetObject: TermVariable, targetField: Identifier, source: TermVariable) extends Statement {
  require(AST.isLegalName(targetField.name), "Field name '" + targetField + "' is no legal Java field name")

  override def allNames = targetObject.allNames ++ source.allNames + targetField.name

  override def rename(renaming: Renaming) = FieldWrite(targetObject.rename(renaming), renaming(targetField), source.rename(renaming))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    require(targetObject != Null, "Can't access fields of 'null' in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")

    typeEnvironment(targetObject.name) match {
      case className:ClassName =>
        program.findField(program.findClassDefinition(className).get, targetField.name) match {
          case Some(field) =>
            require(source == Null || program.checkSubclass(typeEnvironment(source.name), field.fieldType),
              "Field and the variable it is assigned in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "' are incompatible!")

            require(className.name == typeEnvironment(This.name).name || field.accessModifier == AccessModifier.PUBLIC,
              "Trying to access private field '" + field.fieldName + "' of class '" + typeEnvironment(targetObject.name).asInstanceOf[ClassName].name + "' externally!")

            typeEnvironment

          case None =>
            throw new IllegalArgumentException("Class '" + className.name + "' doesn't have field '" + targetField + "' written in class '" +
              typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
        }
      case _ =>
        throw new IllegalArgumentException("Class 'Object' doesn't have field '" + targetField + "' written in class '" +
          typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) = {
    val variablesGraph = targetObject.resolveVariableNames(methodEnvironment) + source.resolveVariableNames(methodEnvironment)

    // Two-step lookup: variable name -> class name -> class environment
    if (typeEnvironment.contains(targetObject.name) && nameEnvironment.contains(typeEnvironment(targetObject.name).name)) {
      val classEnv = nameEnvironment(typeEnvironment(targetObject.name).name)
      val fieldMap = classEnv.map(_._2).filter(_.contains(targetField.name))

      (variablesGraph + NameGraphExtended(Set(targetField), Map(targetField -> fieldMap.flatMap(_(targetField.name)))), (methodEnvironment, typeEnvironment))
    }
    else {
      // Return ID without references if lookup fails
      (variablesGraph + NameGraphExtended(Set(targetField), Map()), (methodEnvironment, typeEnvironment))
    }
  }

  override def toString = targetObject.toString + "." + targetField.toString + " = " + source.toString + ";"
}
