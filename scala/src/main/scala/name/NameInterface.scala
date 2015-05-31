package name

trait NameInterface {
  val moduleID: Identifier

  def export: Set[Identifier]

  def original: NameInterface = {
    val originalMapping = export.map(n => (n, n.rename(n.originalName))).toMap
    rename(originalMapping)
  }

  def rename(renaming: Renaming): NameInterface

  def rename(renaming: Map[Identifier, Name]): NameInterface =
    rename(name => renaming.get(name) match {
      case None => name
      case Some(name2) => name.rename(name2)
    })
}
