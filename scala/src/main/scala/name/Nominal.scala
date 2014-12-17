package name

import name.Name.ID

/**
 * Created by seba on 01/08/14.
 */
trait Nominal {
  type RenamingFunction = Name => Name
  type DependencyRenamingFunction = (Name.ID, Name) => Name

  def allNames: Nodes
  def rename(renaming: RenamingFunction): Nominal
  def resolveNames(): NameGraph

  def rename(renaming: Renaming): Nominal =
    rename(name => renaming.get(name.id) match {
      case None => name
      case Some(name2) => {
        val newID = new ID(name)
        val newName = new Name(name2, newID)
        newID.nameO = newName
        newName
      }
    })
}

trait NominalModular extends Nominal {
  override def resolveNames(): NameGraphModular = resolveNames(Map[Dependency, String]())
  def resolveNames(dependencyRenaming : DependencyRenamingFunction) : NameGraphModular
  def resolveNames(dependencyRenaming : DependencyRenaming) : NameGraphModular =
    resolveNames((graph, name) => dependencyRenaming.get((graph, name.id)) match {
      case None => name
      case Some(name2) => new Name(name2, name.id)
    })
  def exportedNames: ExportedNames
  def safelyQualifiedReference(reference: Name, declaration: Name.ID): Option[NominalModular]
}