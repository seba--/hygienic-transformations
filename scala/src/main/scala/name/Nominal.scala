package name

import NameGraph._

/**
 * Created by seba on 01/08/14.
 */
trait Nominal {
  type Renaming = Name => Name

  def allNames: Set[Name.ID]
  def rename(renaming: Renaming): Nominal
  def resolveNames: NameGraph

  def rename(renaming: Map[Name.ID, String]): Nominal =
    rename(name => renaming.get(name.id) match {
      case None => name
      case Some(name2) => Name(name2)
    })
}
