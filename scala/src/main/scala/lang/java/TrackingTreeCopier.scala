package lang.java

import com.sun.tools.javac.tree.{JCTree, TreeCopier, TreeMaker}

class TrackingTreeCopier[P](tm: TreeMaker) extends TreeCopier[P](tm) {
  private var originTracking = Map[JCTree, JCTree]()

  def originMap = originTracking

  def setOrigin[T <: JCTree](t: T, origin: JCTree): T = {
    originTracking += t -> origin
    t
  }

  override def copy[T <: JCTree](t: T, p: P): T = {
    val t2 = super.copy(t, p)
    setOrigin(t2, t)
    t2
  }
}
