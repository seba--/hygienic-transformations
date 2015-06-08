package name

trait NameInterface {
  //  Module ID the interface belongs to
  val moduleID: Identifier

  // Set of exported identifiers contained in the interface
  def export: Set[Identifier]

  // Restores the original interface before any renamings were applied
  var original: NameInterface = this

  // Renames the identifiers contained in the interface and creates a new interface with the resulting naming
  def rename(renaming: Renaming): NameInterface

  def rename(renaming: Map[Identifier, Name]): NameInterface =
    rename(name => renaming.get(name) match {
      case None => name
      case Some(name2) => name.rename(name2)
    })
}
