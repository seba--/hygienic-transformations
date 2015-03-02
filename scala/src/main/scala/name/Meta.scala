package name

trait Meta {
  val moduleID: Identifier

  def export: Set[Identifier]
}
