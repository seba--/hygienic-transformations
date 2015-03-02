package lang.lightweightjava.whileloops

import lang.lightweightjava.ast._
import lang.lightweightjava.ast.returnvalue.ReturnVariable
import lang.lightweightjava.ast.statement._
import lang.lightweightjava.whileloops.ast.WhileLoop
import name.Identifier

object WhileLoopTransformation {
  def transform(program : Program) = {
    // Transform each class of the program separately, keeping a running counter of added helper methods
    Program(program.classes.foldLeft((Seq[ClassDefinition](), 0))((oldResult, cl) => {
      val transformedClass = transformClass(cl, oldResult._2)
      (oldResult._1 :+ transformedClass._1, transformedClass._2)
    })._1:_*)
  }

  private def transformClass(classDefinition : ClassDefinition, loopCount : Int) = {
    // Transform each method of the class separately, keeping a running counter of added helper methods
    classDefinition.methods.foldLeft((classDefinition, loopCount))(
      (oldResult, method) => transformMethod(oldResult._1, method, oldResult._2))
  }

  private def transformMethod(classDefinition : ClassDefinition, method : MethodDefinition, loopCount : Int) = {
    // Transform all statements of the method separately, keeping a running counter of added helper methods
    val transformationResult = transformMethodRecursive(classDefinition, method, method.methodBody.statements, loopCount)
    // Rebuild the method with the transformed body
    val transformedMethod = MethodDefinition(method.signature, MethodBody(method.methodBody.returnValue, transformationResult._1:_*))
    // Add the transformed method to the class definition and remove the previous one
    (ClassDefinition(classDefinition.className, classDefinition.superClass, transformationResult._2.elements.filterNot(_ == method) :+ transformedMethod:_*), transformationResult._3)
  }

  private def transformMethodRecursive(classDefinition : ClassDefinition, method : MethodDefinition, remainingStatements : Seq[Statement], loopCount : Int) : (Seq[Statement], ClassDefinition, Int) = {
    if (remainingStatements.isEmpty) (Seq(), classDefinition, loopCount)
    // Only while loops and blocks containing sub-statements need separate handling
    else remainingStatements.head match {
      case WhileLoop(leftVariable, rightVariable, loopBody) =>
        // Create a set of helper fields added to transfer the helper method results back to the method
        val addedFields = method.signature.parameters.foldLeft((Set[(VariableDeclaration, FieldDeclaration)](), loopCount))((oldState, param) =>
          (oldState._1 + ((param, FieldDeclaration(AccessModifier.PRIVATE, param.variableType, Identifier("loop" + oldState._2 + "_" + param.variableName.name)))),
            oldState._2 + 1))

        // Create a set of save/restore statements added to transfer the helper method results back to the method
        val saveStatements = addedFields._1.map(field => FieldWrite(This, field._2.fieldName, field._1.variableName))
        val restoreStatements = addedFields._1.map(field => FieldRead(field._1.variableName, This, field._2.fieldName))

        // The while loop is transformed to a conditional branch with the remaining statements as if-branch
        val ifBranch = StatementBlock(remainingStatements.tail:_*)

        // Create a helper method that is called recursively at the end of the else-branch
        val loopMethodName = Identifier(method.signature.methodName.name + "_loop" + addedFields._2)

        val recursiveCallParameters = method.signature.parameters.map(param => param.variableName)
        val recursiveCall = VoidMethodCall(This, loopMethodName.fresh, recursiveCallParameters:_*)

        // The else-branch contains the loop body and a recursive call at the end
        val elseBranch = StatementBlock(Seq(loopBody, recursiveCall) ++ restoreStatements:_*)

        // Build the whole conditional branch
        val conditionalBranch = ConditionalBranch(leftVariable, rightVariable, ifBranch, elseBranch)

        // Build the helper method from the parts above
        val loopMethod = MethodDefinition(MethodSignature(AccessModifier.PRIVATE, ObjectClass, loopMethodName.fresh, method.signature.parameters:_*),
          MethodBody(ReturnVariable(Null), Seq(conditionalBranch) ++ saveStatements:_*))

        // Add the helper method and fields to the class definition
        val newClassDefinition = ClassDefinition(classDefinition.className, classDefinition.superClass,
          classDefinition.elements ++ addedFields._1.map(_._2) :+ loopMethod:_*)

        // Transform the created helper method again as it may contain more loops that were nested into this one
        val newMethodTransformed = transformMethod(newClassDefinition, loopMethod, addedFields._2 + 1)

        // The current method ends with the first recursive call of the helper method and restoring the saved values from the fields
        (Seq(recursiveCall) ++ restoreStatements, newMethodTransformed._1, newMethodTransformed._2)

      case ConditionalBranch(leftVariable, rightVariable, ifBranch, elseBranch) =>
        // The bodies of the conditional branches need to be handled separately as if they were individual sub-methods
        val ifTransformed = transformMethodRecursive(classDefinition, method, Seq(ifBranch), loopCount)
        val elseTransformed = transformMethodRecursive(ifTransformed._2, method, Seq(elseBranch), ifTransformed._3)
        val recursiveResult = transformMethodRecursive(elseTransformed._2, method, remainingStatements.tail, elseTransformed._3)
        (ConditionalBranch(leftVariable, rightVariable,
          if (ifTransformed._1.length > 1) StatementBlock(ifTransformed._1:_*)
          else ifTransformed._1.head,
          if (elseTransformed._1.length > 1) StatementBlock(elseTransformed._1:_*)
          else elseTransformed._1.head) +: recursiveResult._1, recursiveResult._2, recursiveResult._3)

      case StatementBlock(statements @ _*) =>
        // The body of the statement block needs to be handled separately as if it was a individual sub-method
        val bodyTransformed = transformMethodRecursive(classDefinition, method, statements, loopCount)
        val recursiveResult = transformMethodRecursive(bodyTransformed._2, method, remainingStatements.tail, bodyTransformed._3)
        (StatementBlock(bodyTransformed._1:_*) +: recursiveResult._1, recursiveResult._2, recursiveResult._3)
      case statement@_ =>
        val recursiveResult = transformMethodRecursive(classDefinition, method, remainingStatements.tail, loopCount)
        (statement +: recursiveResult._1, recursiveResult._2, recursiveResult._3)

    }
  }
}