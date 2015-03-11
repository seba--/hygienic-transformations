package name

import name.namegraph.NameGraphModular

trait NominalModular[T <: Meta] extends Nominal {
  val moduleID: Identifier

  def dependencies: Set[Name]

  override def rename(renaming: Renaming): NominalModular[T]

  override def rename(renaming: Map[Identifier, Name]): NominalModular[T] =
    rename(name => renaming.get(name) match {
      case None => name
      case Some(name2) => name.rename(name2)
    })

  def resolveNamesModular(dependencies: Set[T]): (NameGraphModular, T)

  def resolveNamesVirtual(dependencies: Set[T], renaming: Renaming): NameGraphModular

  def resolveNamesVirtual(metaDependencies: Set[T], renaming: Map[Identifier, Name]): NameGraphModular =
    resolveNamesVirtual(metaDependencies, name => renaming.get(name) match {
      case None => name
      case Some(name2) => name.rename(name2)
    })
}