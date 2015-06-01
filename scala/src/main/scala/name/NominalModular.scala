package name

import name.namegraph.{NameGraphExtended, NameGraphModular}

trait NominalModular[I <: NameInterface] {
  // Own module ID
  val moduleID: Identifier

  // Module IDs that need to be present for name resolution
  def dependencies: Set[Identifier]

  def allNames: Set[Name]

  def rename(renaming: Renaming): NominalModular[I]

  def rename(renaming: Map[Identifier, Name]): NominalModular[I] =
    rename(name => renaming.get(name) match {
      case None => name
      case Some(name2) => name.rename(name2)
    })

  // Modular name resolution for the given set of interfaces to link against
  def resolveNamesModular(dependencies: Set[I] = Set()): NameGraphModular[I]
}