package ref

import name.Identifier.ID
import name.{Gensym, Nominal}

trait Structural {
  def resolveRefs: RefGraph
  def retarget(retargeting: Map[Reference, Option[Declaration]]): Structural

  def equiv(obj: Structural): Boolean = equiv(obj, Map())
  def equiv(obj: Structural, eqDecls: Map[ID, Declaration]): Boolean

  def asNominal(implicit gensym: Gensym): Nominal
}
