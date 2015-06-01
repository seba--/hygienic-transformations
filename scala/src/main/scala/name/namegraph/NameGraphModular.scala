package name.namegraph

import name.namegraph.NameGraphModular.{Edges, Nodes}
import name.{Identifier, NameInterface}

import scala.language.implicitConversions

object NameGraphModular {
  type Nodes = Set[Identifier]
  type Edges = Map[Identifier, Set[Identifier]]
}

case class NameGraphModular[I <: NameInterface](V: Nodes, IUsed: Set[I], E: Edges, I: I)
