package name

import scala.language.implicitConversions

object NameGraphModular {
  def apply(ID: String, E: Edges): NameGraph = {
    var V: Nodes = Set()
    E foreach { kv =>
      V += kv._1
      V += kv._2
    }
    NameGraphModular(ID, V, E, Map(), Set())
  }

  def apply(ID: String, V: Set[Name.ID], E: Edges) : NameGraph = {
    NameGraphModular(ID, V.map(id => (id, false)), E, Map(), Set())
  }

  def apply(ID: String, V: Nodes, E: Edges, C: DeclarationConflicts) : NameGraph = {
    NameGraphModular(ID, V, E, Map(), C)
  }
}

case class NameGraphModular(ID: String, V: Nodes, E: Edges, EOut: OutEdges,  C: DeclarationConflicts = Set[Set[Name.ID]]()) extends NameGraph {
  def ++(g: NameGraph) = g match {
    case NameGraphModular(id, v, e, eOut, c) => NameGraphModular(ID, V ++ v, E ++ e, EOut ++ eOut, C ++ c)
    case _ => NameGraphModular(ID, V ++ g.V, E ++ g.E, EOut, C ++ g.C)

  }
  def --(g: NameGraph) = g match {
    case NameGraphModular(id, v, e, eOut, c) => NameGraphModular(ID, V -- v, E -- e.keys, EOut -- eOut.keys, C -- c)
    case _ => NameGraphModular(ID, V -- g.V, E -- g.E.keys, EOut, C -- g.C)

  }
  def +(e: Edges) = NameGraphModular(ID, V, E ++ e, EOut, C)

}


