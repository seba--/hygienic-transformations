package name.namegraph

import name.Identifier
import name.namegraph.NameGraphExtended.Nodes

import scala.language.implicitConversions

object NameGraphExtended {
  type Nodes = Set[Identifier]

  def apply(E: Map[Identifier, Set[Identifier]]): NameGraphExtended = {
    var V: Nodes = Set()
    E foreach { kv =>
      V += kv._1
      V ++= kv._2
    }
    NameGraphExtended(V, E)
  }

  implicit def apply(g: NameGraph): NameGraphExtended = {
    new NameGraphExtended(g.V, g.E.map(e => (e._1, Set(e._2))))
  }

  implicit def apply(g: NameGraphExtended): NameGraph = {
    new NameGraph(g.V, g.E.filter(_._2.size > 0).map(e => (e._1,
      if (e._2.size == 1) e._2.head
      else throw new IllegalArgumentException("Can't convert extended name graph with multi-references to simple name graph!"))))
  }
}

case class NameGraphExtended(V: Nodes, E: Map[Identifier, Set[Identifier]]) {
  def +(g: NameGraphExtended) = NameGraphExtended(V ++ g.V,
    (E -- g.E.keys) ++ g.E.map(e => if (E.contains(e._1)) (e._1, E(e._1) ++ e._2) else (e._1, e._2)))
}