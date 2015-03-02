package lang.lightweightjava.configuration

import lang.lightweightjava.ast.{AST, Heap, Program, State}
import name.Name

case class ExceptionConfiguration(program: Program, state: State, heap: Heap, exception: LangException) extends Configuration {
  override def freshName(usedNames: Set[Name], oldName: Name) =
    AST.genFreshName(program.allNames.map(_.name) ++ state.keys.flatMap(_.allNames).map(_.name) ++ usedNames, oldName)

  override def toString = program.toString + "has encountered an exception: " + exception.toString + "\n\n" +
    "(State: " + state.toString + ",\nHeap: " + heap.toString + ")"

}
