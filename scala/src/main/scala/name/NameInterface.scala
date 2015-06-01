package name

trait NameInterface {
  //  Module ID the interface belongs to
  val moduleID: Identifier

  // Set of exported identifiers contained in the interface
  def export: Set[Identifier]

  // Restores the original interface before any renamings were applied
  // As identifiers already store their original name, this method is just a wrapper for renaming the interface to these stored names
  def original: NameInterface = {
    val originalMapping = export.map(n => (n, n.rename(n.originalName))).toMap
    rename(originalMapping)
  }

  // Renames the identifiers contained in the interface and creates a new interface with the resulting naming
  def rename(renaming: Renaming): NameInterface

  def rename(renaming: Map[Identifier, Name]): NameInterface =
    rename(name => renaming.get(name) match {
      case None => name
      case Some(name2) => name.rename(name2)
    })
}
