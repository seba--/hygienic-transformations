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
    (E -- g.E.keys) ++ g.E.map(e => if (E.contains(e._1)) (e._1, E(e._1) ++ e._2) else (e._1, e._2)),
    (EOut -- g.EOut.keys) ++ g.EOut.map(e => if (EOut.contains(e._1)) (e._1, EOut(e._1) ++ e._2) else (e._1, e._2)))
}