package name

/**
 * Created by seba on 01/08/14.
 */
object NameGraph {
  type Nodes = Set[Name.ID]
  type Edges = Map[Name.ID,Name.ID]
  case class NameGraph(V: Nodes, E: Edges) {
    def +(g: NameGraph) = NameGraph(V ++ g.V, E ++ g.E)
  }
}
