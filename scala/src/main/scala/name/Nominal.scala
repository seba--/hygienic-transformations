package name

import NameGraph._

/**
 * Created by seba on 01/08/14.
 */
trait Nominal {
  type Renaming = Name => Name

  def allNames: Set[Name]
  def rename(renaming: Renaming): Nominal
  def resolveNames: NameGraph

  def rename(renaming: Map[Name.ID, Name]): Nominal =
    rename(name => renaming.getOrElse(name.id,name))
  def renameIDs(renaming: Map[Name.ID, Name.ID]): Nominal =
    rename(name => renaming.getOrElse(name.id, name.id).nameO)
}
