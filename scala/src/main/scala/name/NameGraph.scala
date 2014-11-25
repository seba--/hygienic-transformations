package name

import scala.language.implicitConversions

/**
 * Created by seba on 01/08/14.
 */

object NameGraph {
  def apply(E: Edges): NameGraph = {
    var V: Nodes = Set()
    E foreach { kv =>
      V += kv._1
      V += kv._2
    }
    NameGraphGlobal(V, E, Set())
  }

  def apply(V: Set[Name.ID], E: Edges) : NameGraph = {
    NameGraphGlobal(V.map(id => (id, false)), E, Set())
  }

  def apply(V: Set[Name.ID], E: Edges, C : DeclarationConflicts) : NameGraph = {
    NameGraphGlobal(V.map(id => (id, false)), E, C)
  }
}

abstract class NameGraph {
  val V : Nodes
  val E : Edges
  val C : DeclarationConflicts

  def ++(g: NameGraph) : NameGraph
  def --(g: NameGraph) : NameGraph
  def +(g: Edges) : NameGraph

}

case class NameGraphGlobal(V: Nodes, E: Edges, C: DeclarationConflicts = Set[Set[Name.ID]]()) extends NameGraph {
  def ++(g: NameGraph) = NameGraphGlobal(V ++ g.V, E ++ g.E, C ++ g.C)
  def --(g: NameGraph) = NameGraphGlobal(V -- g.V, E -- g.E.keys, C -- g.C)
  def +(e: Edges) = NameGraphGlobal(V, E ++ e, C)

}
