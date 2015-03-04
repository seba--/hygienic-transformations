package lang.lightweightjava.ast.returnvalue

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.statement.{Null, TermVariable, This}
import name.namegraph.NameGraphExtended
import name.{Identifier, Renaming}

case class ReturnField(returnObject: TermVariable, returnField: Identifier) extends ReturnValue {
  require(AST.isLegalName(returnField.name), "Field name '" + returnField + "' is no legal Java field name")

  override def allNames = returnObject.allNames + returnField.name

  override def rename(renaming: Renaming) = ReturnField(returnObject.rename(renaming), renaming(returnField))

  override def typeCheckForTypeEnvironment(program: Program, typeEnvironment: TypeEnvironment, returnType : ClassRef) = {
    require(returnObject != Null, "Can't access fields of 'null' in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
    typeEnvironment(returnObject.name) match {
      case className:ClassName => program.findField(program.findClassDefinition(className).get, returnField.name) match {
        case Some(field) => require(program.checkSubclass(field.fieldType, returnType),
          "Field returned by a method in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "' is incompatible with the method return type!")
          require(className.name == typeEnvironment(This.name).name || field.accessModifier == AccessModifier.PUBLIC,
            "Trying to access private field '" + field.fieldName + "' of class '" + typeEnvironment(returnObject.name).asInstanceOf[ClassName].name + "' externally!")
          typeEnvironment
        case None =>
          throw new IllegalArgumentException("Class '" + className.name + "' doesn't have field '" + returnField + "' returned in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
      }
      case _ => throw new IllegalArgumentException("Class 'Object' doesn't have field '" + returnField + "' returned in class '" + typeEnvironment(This.name).asInstanceOf[ClassName].name + "'")
    }
  }

  override def resolveNames(nameEnvironment: ClassNameEnvironment, methodEnvironment: VariableNameEnvironment, typeEnvironment : TypeEnvironment) = {
    val variablesGraph = returnObject.resolveVariableNames(methodEnvironment)

    if (typeEnvironment.contains(returnObject.name) && nameEnvironment.contains(typeEnvironment(returnObject.name).name)) {
      val fieldMap = nameEnvironment(typeEnvironment(returnObject.name).name).map(_._2).filter(_.contains(returnField.name))

      variablesGraph + NameGraphExtended(Set(returnField), Map(returnField -> fieldMap.flatMap(_(returnField.name))))
    }
    else {
      variablesGraph + NameGraphExtended(Set(returnField), Map())
    }
  }

  override def toString = returnObject.toString + "." + returnField.toString
}