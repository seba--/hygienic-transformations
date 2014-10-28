package lang.lightweightjava

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.returnvalue._
import lang.lightweightjava.ast.statement._
import lang.lightweightjava.configuration._
import name.Name

object Interpreter {
  def interpret(configuration: Configuration): Configuration = {
    // Type checking will raise an error if there are type errors, so if the call returns,
    // it can be assumed that the program is correctly typed.
    // Please also note that only the program is type checked, so type errors in the program flow
    // might cause undefined behavior or runtime errors. This is as defined by LJ specification.
    configuration.program.typeCheck

    // Using a separate method here to avoid unnecessary type checks after each interpretation step.
    interpretInternal(configuration)
  }

  private def interpretInternal(configuration: Configuration): Configuration = {
    configuration match {
      case NormalConfiguration(program, state, heap, programFlow@_*) =>
        if (programFlow.isEmpty) configuration
        else {

          // Temporary variables for configuration parameters that can be overwritten
          // and are used for the next interpretation step
          var newState = state + (Null -> NullValue)
          var newHeap = heap
          var newProgramFlow = programFlow.tail
          var exception: LangException = null

          programFlow.head match {
            case ConditionalBranch(leftVariable, rightVariable, ifBranch, elseBranch) =>
              if (state(leftVariable) == state(rightVariable)) newProgramFlow = ifBranch +: newProgramFlow
              else newProgramFlow = elseBranch +: newProgramFlow

            case FieldRead(target, sourceObject, sourceField) => newState(sourceObject) match {
              case oid@OID(_) =>
                val (_, fields) = newHeap(oid)
                newState = newState + (target -> fields(sourceField))
              case NullValue => exception = NullPointerException
            }

            case FieldWrite(targetObject, targetField, source) => newState(targetObject) match {
              case oid@OID(_) =>
                val (objectType, fields) = newHeap(oid)
                val updatedFieldMap = fields + (targetField -> newState(source))
                newHeap = newHeap + (oid -> (objectType, updatedFieldMap))
              case NullValue => exception = NullPointerException
            }

            case MethodCall(target, sourceObject, methodName, methodParameters@_*) => newState(sourceObject) match {
              case oid@OID(_) =>
                val (objectType, _) = newHeap(oid)
                val methodDefinition = program.findMethod(program.getClassDefinition(objectType.asInstanceOf[ClassName]).get, methodName).get
                methodDefinition match {
                  case MethodDefinition(MethodSignature(_, _, _, parameters@_*), methodBody) =>
                    // Generate a map with fresh names for each method parameter
                    var renamedParameters = parameters.foldLeft(Map[Name, Name]())((renamedMap, parameter) =>
                      renamedMap + (parameter.name.variableName -> configuration.freshName(renamedMap.values.map(_.name).toSet, parameter.name.variableName)))
                    // Generate a fresh name for "this"
                    val thisRenaming = configuration.freshName(renamedParameters.values.map(_.name).toSet, This.variableName)
                    renamedParameters = renamedParameters + (This.variableName -> thisRenaming)
                    // Perform the renaming to fresh names
                    def parameterRenaming = (name : Name) => renamedParameters.getOrElse(name, name)
                    val renamedMethodBody = methodBody.rename(parameterRenaming)
                    // Add the values used for the call to the stack (and let "this" point to the OID of the method owning object)
                    newState = newState ++ parameters.
                      zip(methodParameters).
                      map(p => (VariableName(renamedParameters(p._1.name.variableName)), newState(p._2))).
                      toMap[TermVariable, Value] + (VariableName(thisRenaming) -> oid)
                    // Add the method body at the front the program flow (and replace this statement by a simple assignment of the return value)
                    newProgramFlow = (renamedMethodBody.statements :+ (renamedMethodBody.returnValue match {
                      case ReturnVariable(returnVariable) => VariableAssignment(target, returnVariable)
                      case ReturnField(returnObject, returnField) => FieldRead(target, returnObject, returnField)
                      case ReturnMethodCall(returnObject, returnMethodName, returnMethodParameters@_*) => MethodCall(target, returnObject, returnMethodName, returnMethodParameters:_*)
                      case ReturnObjectInstantiation(returnClassRef) => ObjectInstantiation(target, returnClassRef)
                    })) ++: newProgramFlow
                }
              case NullValue => exception = NullPointerException
            }

            // See MethodCall case for documentation (only difference is the missing assignment of the return value at the end)
            case VoidMethodCall(sourceObject, methodName, methodParameters@_*) => newState(sourceObject) match {
              case oid@OID(_) =>
                val (objectType, _) = newHeap(oid)
                val methodDefinition = program.findMethod(program.getClassDefinition(objectType.asInstanceOf[ClassName]).get, methodName).get
                methodDefinition match {
                  case MethodDefinition(MethodSignature(_, returnVariable, _, parameters@_*), methodBody) =>
                    var renamedParameters = parameters.foldLeft(Map[Name, Name]())((renamedMap, parameter) =>
                      renamedMap + (parameter.name.variableName -> configuration.freshName(renamedMap.values.map(_.name).toSet, parameter.name.variableName)))
                    val thisRenaming = configuration.freshName(renamedParameters.values.map(_.name).toSet, This.variableName)
                    renamedParameters = renamedParameters + (This.variableName -> thisRenaming)
                    def parameterRenaming = (name : Name) => renamedParameters.getOrElse(name, name)
                    val renamedMethodBody = methodBody.rename(parameterRenaming)
                    newState = newState ++ parameters.
                      zip(methodParameters).
                      map(p => (VariableName(renamedParameters(p._1.name.variableName)), newState(p._2))).
                      toMap[TermVariable, Value] + (VariableName(thisRenaming) -> oid)
                    newProgramFlow = renamedMethodBody.statements ++: newProgramFlow
                }
              case NullValue => exception = NullPointerException
            }

            case ObjectInstantiation(target, classRef) =>
              val newValue = OID(configuration.freshOID())
              val newFieldsList: Map[Name, Value] = classRef match {
                case className@ClassName(name) =>
                  val classFields = program.getClassFields(program.getClassDefinition(className).get)
                  // All class fields are initialized as "null"
                  classFields.map(field => (field.fieldName, NullValue)).toMap[Name, Value]
                // Object class doesn't have fields
                case _ => Map()
              }
              val newHeapEntry = (classRef, newFieldsList)
              newState = newState + (target -> newValue)
              newHeap = newHeap + (newValue -> newHeapEntry)

            case StatementBlock(blockBody@_*) => newProgramFlow = blockBody ++: newProgramFlow

            case VariableAssignment(target, source) => newState = newState + (target -> newState(source))
          }

          if (exception == null) interpretInternal(NormalConfiguration(program, newState, newHeap, newProgramFlow: _*))
          else ExceptionConfiguration(program, newState, newHeap, exception)
        }

      case ExceptionConfiguration(program, state, heap, exception) => sys.error("Can't interpret exception configuration: " + exception.message)
    }
  }
}
