package name

/**
 * Created by seba on 01/08/14.
 */
trait Nominal {
  type Renaming = Name => Name
  type DependencyRenaming = Name => Name

  def allNames: Set[Name.ID]
  def rename(renaming: Renaming): Nominal
  def resolveNames(): NameGraph

  def rename(renaming: Map[Name.ID, String]): Nominal =
    rename(name => renaming.get(name.id) match {
      case None => name
      case Some(name2) => new Name(name2, name.id)
    })
}

trait NominalModular extends Nominal {
  override def resolveNames(): NameGraphModular = resolveNames(Map[Name.ID, String]())
  def resolveNames(dependencyRenaming : DependencyRenaming) : NameGraphModular
  def resolveNames(dependencyRenaming : Map[Name.ID, String]) : NameGraphModular =
    resolveNames(name => dependencyRenaming.get(name.id) match {
      case None => name
      case Some(name2) => new Name(name2, name.id)
    })
  def exportedNames: Set[Name]
  def safelyQualifiedReference(reference: Name, declaration: Name.ID): Option[NominalModular]
}