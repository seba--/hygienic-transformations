package lang.lightweightjava.ast.statement

import lang.lightweightjava.ast._
import name.namegraph.NameGraphExtended
import name.{Identifier, Renaming}

case class FieldRead(target: VariableName, sourceObject: TermVariable, sourceField: Identifier) extends Statement {
  require(AST.isLegalName(sourceField.name), "Field name '" + sourceField + "' is no legal Java field name")

  override def allNames = sourceObject.allNames ++ target.allNames + sourceField.name

  override def rename(renaming: Renaming) = FieldRead(target.rename(renaming).asInstanceOf[VariableName], sourceObject.rename(renaming), renaming(sourceField))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment) = {
    require(sourceObject != Null, "Can't access fields of 'null' in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")

    typeEnvironment(sourceObject.name) match {
      case className:ClassName =>
        program.findField(program.findClassDefinition(className).get, sourceField.name) match {
          case Some(field) =>
            require(program.checkSubclass(field.fieldType, typeEnvironment(target.name)),
              "Variable and the field it is assigned in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "' are incompatible!")

            require(className.name == typeEnvironment(This.name).name || field.accessModifier == AccessModifier.PUBLIC,
              "Trying to access private field '" + field.fieldName + "' of class '" + typeEnvironment(sourceObject.name).asInstanceOf[ClassName].name + "' externally!")

            typeEnvironment

          case None =>
            throw new IllegalArgumentException("Class '" + className.name + "' doesn't have field '" + sourceField + "' read in class '" +
              typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
        }
      case _ =>
        throw new IllegalArgumentException("Class 'Object' doesn't have field '" + sourceField + "' read in class '" +
          typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) = {
    val variablesGraph = target.resolveVariableNames(methodEnvironment) + sourceObject.resolveVariableNames(methodEnvironment)

    // Two-step lookup: variable name -> class name -> class environment
    if (typeEnvironment.contains(sourceObject.name) && nameEnvironment.contains(typeEnvironment(sourceObject.name).name)) {
      val classEnv = nameEnvironment(typeEnvironment(sourceObject.name).name)
      val fieldMap = classEnv.map(_._2).filter(_.contains(sourceField.name))

      (variablesGraph + NameGraphExtended(Set(sourceField), Map(sourceField -> fieldMap.flatMap(_(sourceField.name)))), (methodEnvironment, typeEnvironment))
    }
    else {
      // Return ID without references if lookup fails
      (variablesGraph + NameGraphExtended(Set(sourceField), Map()), (methodEnvironment, typeEnvironment))
    }
  }

  override def toString = target.toString + " = " + sourceObject.toString + "." + sourceField.toString + ";"
}
