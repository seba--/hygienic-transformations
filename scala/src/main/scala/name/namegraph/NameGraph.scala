package name.namegraph

import name.Identifier

/**
 * Created by seba on 01/08/14.
 */

object NameGraph {
  type Nodes = Set[Identifier]
  type Edges = Map[Identifier, Identifier]

  def apply(E: Edges): NameGraph = {
    var V: Nodes = Set()
    E foreach { kv =>
      V += kv._1
      V += kv._2
    }
    NameGraph(V, E)
  }
}

import name.namegraph.NameGraph._

case class NameGraph(V: Nodes, E: Edges) {
  def +(g: NameGraph) = NameGraph(V ++ g.V, E ++ g.E)
}