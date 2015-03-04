package name

import name.namegraph.NameGraphExtended

/**
 * Created by seba on 01/08/14.
 */
trait Nominal {

  def allNames: Set[Name]
  def rename(renaming: Renaming): Nominal
  def resolveNames: NameGraphExtended

  def rename(renaming: Map[Identifier, Name]): Nominal =
    rename(name => renaming.get(name) match {
      case None => name
      case Some(name2) => Identifier(name2)
    })
}