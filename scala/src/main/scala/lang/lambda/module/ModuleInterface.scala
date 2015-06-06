package lang.lambda.module

import name.{Renaming, NameInterface, Identifier}

case class ModuleInterface(moduleID: Identifier, exportedDefs: Set[Identifier]) extends NameInterface {
  // Renames the identifiers contained in the interface and creates a new interface with the resulting naming
  override def rename(renaming: Renaming): NameInterface = ModuleInterface(moduleID, exportedDefs.map(renaming(_)))

  // Set of exported identifiers contained in the interface
  override def export: Set[Identifier] = exportedDefs
}
