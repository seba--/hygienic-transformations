package name

trait MetaInterface {
  val moduleID: Identifier

  def export: Set[Identifier]
}
