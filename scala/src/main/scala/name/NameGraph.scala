package name

import name.NameGraph.{DeclarationConflicts, Nodes, Edges}

import scala.language.implicitConversions

/**
 * Created by seba on 01/08/14.
 */

object NameGraph {
  type Nodes = Set[(Name.ID, Boolean)]

  implicit def nodeFromID(id : Name.ID) : (Name.ID, Boolean) = (id, false)
  implicit def nodeToID(node : (Name.ID, Boolean)) : Name.ID = node._1

  type Edges = Map[Name.ID, Name.ID]
  type DeclarationConflicts = Set[Set[Name.ID]]

  def apply(E: Edges): NameGraph = {
    var V: Nodes = Set()
    E foreach { kv =>
      V += kv._1
      V += kv._2
    }
    NameGraph(V, E, Set())
  }

  def apply(V: Set[Name.ID], E: Edges) : NameGraph = {
    NameGraph(V.map(id => (id, false)), E, Set())
  }
}

case class NameGraph(V: Nodes, E: Edges, C: DeclarationConflicts = Set[Set[Name.ID]]()) {
  def +(g: NameGraph) = NameGraph(V ++ g.V, E ++ g.E, C ++ g.C)
  def -(g: NameGraph) = NameGraph(V -- g.V, E -- g.E.keys, C -- g.C)
}
