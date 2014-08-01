package name

import scala.language.implicitConversions

/**
 * Created by seba on 01/08/14.
 */
object NameGraph {
  class ID(val name: String) {
    override def toString = name.toString
    def fresh = new ID(name)
  }
  object ID {
    implicit def apply(s: String) = new ID(s)
  }

  type Nodes = Set[ID]
  type Edges = Map[ID,ID]
  case class NameGraph(V: Nodes, E: Edges) {
    def +(g: NameGraph) = NameGraph(V ++ g.V, E ++ g.E)
  }

  def freshID(s: String) = new String(s)
}

//object StringNameGraph extends NameGraph[String] {
//  def makeID(s: String) = new String(s)
//}