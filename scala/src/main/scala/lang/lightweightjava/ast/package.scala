package lang.lightweightjava

import lang.lightweightjava.ast.statement.TermVariable
import lang.lightweightjava.configuration.Value
import name.{Identifier, Name}

package object ast {
  type State = Map[TermVariable, Value]

  def State(): State = Map()

  type Heap = Map[Value, (ClassRef, Map[Name, Value])]

  def Heap(): Heap = Map()

  type TypeEnvironment = Map[TermVariable, ClassRef]

  // Class name -> Set[Class, (Field name -> Fields), (Method name -> Methods)]
  type ClassNameEnvironment = Map[Name, Set[(ClassName, Map[Name, Set[Identifier]], Map[Name, Set[Identifier]])]]

  // Variable name -> Variable declaration
  type VariableNameEnvironment = Map[Name, Identifier]
}