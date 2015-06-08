package lang.lightweightjava.trans.privatefield

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.returnvalue.{ReturnVariable, ReturnValue, ReturnMethodCall, ReturnField}
import lang.lightweightjava.ast.statement._
import name.Identifier

object MakeFieldPrivateTransformation {
  def transform(classDefinition: ClassDefinition, fieldName: Identifier, program: Program): ClassDefinition = {
    val nameEnvironment = program.nameEnvironment

    var newMethods = Set[MethodDefinition]()
    for (method <- classDefinition.methods) {
      val methodEnvironment = method.typeEnvironment(classDefinition)
      val newStatements = method.methodBody.statements.foldLeft(Seq[Statement]())((prev, next) => prev :+ (next match {
        case st@FieldRead(target, sourceObject, sourceField) =>
          if (nameEnvironment(methodEnvironment(sourceObject.name).name).exists(_._2(sourceField.name)contains(fieldName)))
            MethodCall(target, sourceObject, generateGetterName(sourceField))
          else
            st

        case st@FieldWrite(targetObject, targetField, source) =>
          if (nameEnvironment(methodEnvironment(targetObject.name).name).exists(_._2(targetField.name).contains(fieldName)))
            VoidMethodCall(targetObject, generateSetterName(targetField), source)
          else
            st

        case st:Statement => st
      }))

      val newReturnValue = method.methodBody.returnValue match {
        case r@ReturnField(returnObject, returnField) =>
          if (nameEnvironment(methodEnvironment(returnObject.name).name).exists(_._2(returnField.name).contains(fieldName)))
            ReturnMethodCall(returnObject, generateGetterName(returnField))
          else
            r

        case r:ReturnValue => r
      }
      newMethods += MethodDefinition(method.signature, MethodBody(newReturnValue, newStatements:_*))
    }

    classDefinition.fields.find(_.fieldName == fieldName) match {
      case Some(field) =>
        val newField = field.copy(AccessModifier.PRIVATE)
        val newGetter = MethodDefinition(MethodSignature(AccessModifier.PUBLIC, field.fieldType, generateGetterName(field.fieldName)),
          MethodBody(ReturnField(This, field.fieldName)))
        val newSetter = MethodDefinition(MethodSignature(AccessModifier.PUBLIC, field.fieldType, generateSetterName(field.fieldName),
          VariableDeclaration(field.fieldType, generateSetterParameter(field.fieldName))),
          MethodBody(ReturnVariable(Null), FieldWrite(This, field.fieldName, generateSetterParameter(field.fieldName))))
        val newElements = Set[ClassElement]() ++ classDefinition.fields - field + newField ++ newMethods + newSetter + newGetter
        ClassDefinition(classDefinition.className, classDefinition.superClass, newElements.toSeq:_*)
      case None =>
        val newElements = Set[ClassElement]() ++ classDefinition.fields ++ newMethods
        ClassDefinition(classDefinition.className, classDefinition.superClass, newElements.toSeq:_*)
    }
  }

  def generateGetterName(fieldName: Identifier): Identifier = {
    Identifier("get" + fieldName.name.capitalize)
  }

  def generateSetterName(fieldName: Identifier): Identifier = {
    Identifier("set" + fieldName.name.capitalize)
  }

  def generateSetterParameter(fieldName: Identifier): VariableName = {
    VariableName(fieldName.name)
  }
}
