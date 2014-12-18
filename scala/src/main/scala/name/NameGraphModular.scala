package name

import scala.language.implicitConversions

object NameGraphModular {
  def apply(ID: Name.ID, E: Edges): NameGraph = {
    var V: Nodes = Set()
    E foreach { kv =>
      V += kv._1
      V += kv._2
    }
    NameGraphModular(ID, V, E, Map(), Set())
  }

  def apply(ID: Name.ID, V: Nodes, E: Edges) : NameGraph = {
    NameGraphModular(ID, V, E, Map(), Set())
  }

  def apply(ID: Name.ID, V: Nodes, E: Edges, C: DeclarationConflicts) : NameGraph = {
    NameGraphModular(ID, V, E, Map(), C)
  }
}

case class NameGraphModular(ID: Name.ID, V: Nodes, E: Edges, EOut: OutEdges, C: DeclarationConflicts = Set[Nodes]()) extends NameGraph {
  override def ++(g: NameGraph) : NameGraphModular = g match {
    case NameGraphModular(id, v, e, eOut, c) => NameGraphModular(ID, V ++ v, E ++ e, EOut ++ eOut, C ++ c)
    case _ => NameGraphModular(ID, V ++ g.V, E ++ g.E, EOut, C ++ g.C)

  }
  override def --(g: NameGraph) : NameGraphModular = g match {
    case NameGraphModular(id, v, e, eOut, c) => NameGraphModular(ID, V -- v, E -- e.keys, EOut -- eOut.keys, C -- c)
    case _ => NameGraphModular(ID, V -- g.V, E -- g.E.keys, EOut, C -- g.C)

  }
  override def +(e: Edges) = NameGraphModular(ID, V, E ++ e, EOut, C)
}


