package lang.lightweightjava.configuration

import lang.lightweightjava.ast.{Heap, Program, State}
import name.Name

abstract class Configuration() {
  def program : Program
  def state : State
  def heap : Heap

  def freshName(usedNames : Set[Name], oldName: Name) : Name

  def freshOID(count : Int = 0) : String = {
    val allValues = state.values ++ heap.keys
    if (allValues.exists {
      case OID(oid) => oid == count.toString
      case _ => false
    }) freshOID(count + 1)
    else count.toString
  }
}
