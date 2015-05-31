package name

import name.namegraph.NameGraphModular

trait NominalModular[I <: NameInterface] extends Nominal {
  val moduleID: Identifier

  def dependencies: Set[Name]

  override def rename(renaming: Renaming): NominalModular[I]

  override def rename(renaming: Map[Identifier, Name]): NominalModular[I] =
    rename(name => renaming.get(name) match {
      case None => name
      case Some(name2) => name.rename(name2)
    })

  def resolveNamesModular(dependencies: Set[I] = Set()): NameGraphModular[I]
}