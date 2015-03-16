package name.namegraph

import name.Identifier
import name.namegraph.NameGraphModular.Nodes

import scala.language.implicitConversions

object NameGraphModular {
  type Nodes = Set[Identifier]

  def apply(E: Map[Identifier, Set[Identifier]]): NameGraphModular = {
    var V: Nodes = Set()
    E foreach { kv =>
      V += kv._1
      V ++= kv._2
    }
    NameGraphModular(V, E, Map())
  }

  implicit def apply(g: NameGraphExtended): NameGraphModular = {
    new NameGraphModular(g.V, g.E, Map())
  }
}

case class NameGraphModular(V: Nodes, E: Map[Identifier, Set[Identifier]], EOut: Map[Identifier, Set[Identifier]]) {
  def +(g: NameGraphModular) = NameGraphModular(V ++ g.V,
    E ++ g.E.map(e => (e._1, E.getOrElse(e._1, Set()) ++ e._2)),
    EOut ++ g.EOut.map(e => (e._1, EOut.getOrElse(e._1, Set()) ++ e._2)))
}