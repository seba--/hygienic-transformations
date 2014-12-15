import scala.language.implicitConversions

package object name {
  type Nodes = Set[Name.ID]
  type Edges = Map[Name.ID, Name.ID]
  type DeclarationConflicts = Set[Set[Name.ID]]
  type OutEdges = Map[Name.ID, (Name.ID, Name.ID)]

  type Meta = (Name.ID, ExportedNames, ExportedNames, Renaming)
  type ExportedNames = Set[Name]
  type Renaming = Map[Name.ID, String]
}
