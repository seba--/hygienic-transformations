import scala.language.implicitConversions

package object name {
  type Nodes = Set[Name.ID]
  type Edges = Map[Name.ID, Name.ID]
  type DeclarationConflicts = Set[Set[Name.ID]]
  type Dependency = (Name.ID, Name.ID)
  type OutEdges = Map[Name.ID, Dependency]

  type Meta = (Name.ID, ExportedNames, ExportedNames, Renaming)
  type ExportedNames = Set[Name]
  type Renaming = Map[Name.ID, String]
  type DependencyRenaming = Map[Dependency, String]
}
