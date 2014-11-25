import scala.language.implicitConversions

package object name {
  type Nodes = Set[(Name.ID, Boolean)]
  type Edges = Map[Name.ID, Name.ID]
  type DeclarationConflicts = Set[Set[Name.ID]]
  type OutEdges = Map[Name.ID, (NameGraphModular.ID, Name.ID)]

  implicit def nodeFromID(id : Name.ID) : (Name.ID, Boolean) = (id, false)
  implicit def nodeToID(node : (Name.ID, Boolean)) : Name.ID = node._1
}
