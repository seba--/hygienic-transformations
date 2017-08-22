package ref

case class RefGraph(decls: Set[Declaration], refs: Set[Reference]) {
  def +(g: RefGraph) = RefGraph(decls ++ g.decls, refs ++ g.refs)
}