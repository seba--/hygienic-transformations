package ref

import name.namegraph.NameGraphExtended

/**
 * Created by seba on 01/08/14.
 */
trait Structural {
  def resolveRefs: RefGraph
  def retarget(retargeting: Map[Reference, Declaration]): Structural
}
