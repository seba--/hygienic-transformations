package name

import scala.language.implicitConversions

object NameGraphModular {
  def apply(ID: String, E: Edges): NameGraph = {
    var V: Nodes = Set()
    E foreach { kv =>
      V += kv._1
      V += kv._2
    }
    NameGraphModular(ID, V, E, Map(), Set(), Set())
  }

  def apply(ID: String, V: Set[Name.ID], E: Edges) : NameGraph = {
    NameGraphModular(ID, V.map(id => (id, false)), E, Map(), Set(), Set())
  }

  def apply(ID: String, V: Nodes, E: Edges, C: DeclarationConflicts) : NameGraph = {
    NameGraphModular(ID, V, E, Map(), C, Set())
  }
}

case class NameGraphModular(ID: String, V: Nodes, E: Edges, EOut: OutEdges, C: DeclarationConflicts = Set[Set[Name.ID]](), COut: ImportConflicts = Set[Set[(String, Name.ID)]]()) extends NameGraph {
  def ++(g: NameGraph) = g match {
    case NameGraphModular(id, v, e, eOut, c, cOut) => NameGraphModular(ID, V ++ v, E ++ e, EOut ++ eOut, C ++ c, COut ++ cOut)
    case _ => NameGraphModular(ID, V ++ g.V, E ++ g.E, EOut, C ++ g.C, COut)

  }
  def --(g: NameGraph) = g match {
    case NameGraphModular(id, v, e, eOut, c, cOut) => NameGraphModular(ID, V -- v, E -- e.keys, EOut -- eOut.keys, C -- c, COut -- cOut)
    case _ => NameGraphModular(ID, V -- g.V, E -- g.E.keys, EOut, C -- g.C, COut)

  }
  def +(e: Edges) = NameGraphModular(ID, V, E ++ e, EOut, C, COut)
}


