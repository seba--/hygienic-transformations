package name

import NameGraph._

/**
 * Created by seba on 01/08/14.
 */
trait Nominal {
  def allIDs: Set[ID]
  def rename(renaming: Map[ID,ID]): Nominal = rename(id => renaming.getOrElse(id,id))
  def rename(renaming: ID => ID): Nominal
  def resolveNames: NameGraph
}
object Nominal {
  def isName(req: ID, was: ID): Option[String] =
    if (req == was) Some(was.name) else None
}