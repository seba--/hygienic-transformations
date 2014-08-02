package name

import NameGraph._

/**
 * Created by seba on 01/08/14.
 */
trait Nominal {
  type Renaming = Name => Name

  def allNames: Set[Name]
  def rename(renaming: Map[Name.ID,Name]): Nominal = rename(name => renaming.getOrElse(name.id,name))
  def rename(renaming: Renaming): Nominal
  def resolveNames: NameGraph
}
object Nominal {
  def isName(req: Name, was: Name): Option[String] =
    if (req == was) Some(was.name) else None
}