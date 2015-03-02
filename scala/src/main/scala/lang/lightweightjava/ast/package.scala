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

  // Class name -> (Class ID, (Field name -> Field ID), (Method name -> Method ID)
  type ClassNameEnvironment = Map[Name, (Identifier, Map[Name, Identifier], Map[Name, Identifier])]

  // Variable name -> Variable declaration ID
  type VariableNameEnvironment = Map[Name, Identifier]
}