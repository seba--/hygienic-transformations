package name

import scala.language.implicitConversions

/**
 * Created by seba on 01/08/14.
 */

object NameGraphModular {
  class ID(var nameGraph: NameGraph)

  def apply(E: Edges): NameGraph = {
    val ID = new ID(null)
    var V: Nodes = Set()
    E foreach { kv =>
      V += kv._1
      V += kv._2
    }
    val nameGraph = NameGraphModular(ID, V, E, Map(), Set())
    ID.nameGraph = nameGraph
    nameGraph
  }

  def apply(V: Set[Name.ID], E: Edges) : NameGraph = {
    val ID = new ID(null)
    val nameGraph = NameGraphModular(ID, V.map(id => (id, false)), E, Map(), Set())
    ID.nameGraph = nameGraph
    nameGraph
  }

  def apply(V: Nodes, E: Edges, C: DeclarationConflicts) : NameGraph = {
    val ID = new ID(null)
    val nameGraph = NameGraphModular(ID, V, E, Map(), C)
    ID.nameGraph = nameGraph
    nameGraph
  }
}

case class NameGraphModular(ID: NameGraphModular.ID, V: Nodes, E: Edges, EOut: OutEdges,  C: DeclarationConflicts = Set[Set[Name.ID]]()) extends NameGraph {
  def ++(g: NameGraph) = NameGraphModular(ID, V ++ g.V, E ++ g.E, EOut, C ++ g.C)
  def --(g: NameGraph) = NameGraphModular(ID, V -- g.V, E -- g.E.keys, EOut, C -- g.C)
  def ++(g: NameGraphModular) = NameGraphModular(ID, V ++ g.V, E ++ g.E, EOut ++ g.EOut, C ++ g.C)
  def --(g: NameGraphModular) = NameGraphModular(ID, V -- g.V, E -- g.E.keys, EOut -- g.EOut.keys, C -- g.C)
  def +(e: Edges) = NameGraphModular(ID, V, E ++ e, EOut, C)

}


