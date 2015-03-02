package name

trait Meta {
  def moduleID: Identifier

  def export: Set[Identifier]

  def reverseRenaming: Map[Identifier, Name]
}
