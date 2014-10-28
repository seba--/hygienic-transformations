package name

import name.NameGraph.{Nodes, Edges, NameGraphError}

import scala.language.implicitConversions

/**
 * Created by seba on 01/08/14.
 */

object NameGraph {
  type Nodes = Set[(Name.ID, Boolean)]

  implicit def nodeFromID(id : Name.ID) : (Name.ID, Boolean) = (id, false)
  implicit def nodeToID(node : (Name.ID, Boolean)) : Name.ID = node._1

  type Edges = Map[Name.ID, Name.ID]

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

  abstract class NameGraphError
  case class UnboundReferenceError(reference : Name.ID) extends NameGraphError
  case class MultipleDeclarationsError(declarations : Set[Name.ID]) extends NameGraphError

}

case class NameGraph(V: Nodes, E: Edges, Err: Set[NameGraphError] = Set[NameGraphError]()) {
  def +(g: NameGraph) = NameGraph(V ++ g.V, E ++ g.E, Err ++ g.Err)
  def -(g: NameGraph) = NameGraph(V -- g.V, E -- g.E.keys, Err -- g.Err)
}
