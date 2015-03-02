package name

import name.namegraph.NameGraphModular

trait NominalModular[T <: Meta] extends Nominal {
  def moduleID: Identifier

  def dependencies: Set[Name]

  override def rename(renaming: Renaming): NominalModular[T]

  override def rename(renaming: Map[Identifier, Name]): NominalModular[T] =
    rename(name => renaming.get(name) match {
      case None => name
      case Some(name2) => Identifier(name2)
    })

  def resolveNamesModular(metaDependencies: Set[T]): (NameGraphModular, T)

  def resolveNamesVirtual(metaDependencies: Set[T], renaming: Renaming): NameGraphModular

  def resolveNamesVirtual(metaDependencies: Set[T], renaming: Map[Identifier, Name]): NameGraphModular =
    resolveNamesVirtual(metaDependencies, name => renaming.get(name) match {
      case None => name
      case Some(name2) => Identifier(name2)
    })
}