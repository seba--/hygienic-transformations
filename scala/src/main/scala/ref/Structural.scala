package ref

trait Structural {
  def resolveRefs: RefGraph
  def retarget(retargeting: Map[Reference, Declaration]): Structural

  def equiv(obj: Structural): Boolean = equiv(obj, Map())
  def equiv(obj: Structural, eqDecls: Map[Declaration, Declaration]): Boolean
}
