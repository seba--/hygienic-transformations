package lang.lightweightjava.ast.returnvalue

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.statement.{Null, TermVariable, This}
import name.namegraph.{NameGraphExtended, NameGraph}
import name.{Identifier, Renaming}

case class ReturnField(returnObject: TermVariable, returnField: Identifier) extends ReturnValue {
  require(AST.isLegalName(returnField.name), "Field name '" + returnField + "' is no legal Java field name")

  override def allNames = returnObject.allNames + returnField

  override def rename(renaming: Renaming) = ReturnField(returnObject.rename(renaming), renaming(returnField))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment, returnType : ClassRef) = {
    require(returnObject != Null, "Can't access fields of 'null' in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
    typeEnvironment(returnObject) match {
      case className@ClassName(_) => program.findField(program.getClassDefinition(className).get, returnField.name) match {
        case Some(field) => require(program.checkSubclass(field.fieldType, returnType),
          "Field returned by a method in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "' is incompatible with the method return type!")
          require(className.name == typeEnvironment(This).name || field.accessModifier == AccessModifier.PUBLIC,
            "Trying to access private field '" + field.fieldName + "' of class '" + typeEnvironment(returnObject).asInstanceOf[ClassName].name + "' externally!")
          typeEnvironment
        case None =>
          throw new IllegalArgumentException("Class '" + className.name + "' doesn't have field '" + returnField + "' returned in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
      }
      case _ => throw new IllegalArgumentException("Class 'Object' doesn't have field '" + returnField + "' returned in class '" + typeEnvironment(This).asInstanceOf[ClassName].name + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) = {
    val variablesGraph = returnObject.resolveVariableNames(methodEnvironment)

    if (typeEnvironment.contains(returnObject) && nameEnvironment.contains(typeEnvironment(returnObject).name)) {
      val fieldMap = nameEnvironment(typeEnvironment(returnObject).name).map(_._2).filter(_.contains(returnField.name))

      variablesGraph + NameGraphExtended(Set(returnField), Map(returnField -> fieldMap.flatMap(_(returnField.name))))
    }
    else {
      variablesGraph + NameGraph(Set(returnField), Map())
    }
  }

  override def toString = returnObject.toString + "." + returnField.toString
}