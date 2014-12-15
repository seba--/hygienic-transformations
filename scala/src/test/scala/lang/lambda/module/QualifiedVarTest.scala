package lang.lambda.module

import lang.lambda._
import lang.lambda.num._
import name._
import org.scalatest._

class QualifiedVarTest extends FunSuite {
  val fixer: NameFix = NameFix.fixerModular

  val baseModule = ModuleNoPrecedence("base", Set(),
    Map((Name("one"), true) -> Num(1)))
  val oneAdderModule = ModuleInternalPrecedence("oneAdder", Set(baseModule),
    Map((Name("two"), true) -> Add(QualifiedVar("base", "one"), Var("one"))))
  val multiAdderModuleIP = ModuleInternalPrecedence("multiAdder", Set(baseModule, oneAdderModule),
    Map((Name("three"), true) -> Add(QualifiedVar("oneAdder","two"), Var("one")),
        (Name("two"), true) -> Add(Var("one"), Var("one"))))
  val multiAdderModuleEP = ModuleExternalPrecedence("multiAdder", Set(baseModule, oneAdderModule),
    Map((Name("three"), true) -> Add(QualifiedVar("multiAdder","two"), Var("one")),
        (Name("two"), true) -> Add(Var("one"), QualifiedVar("base", "one"))))
  val multiAdderModuleIP2 = ModuleInternalPrecedence("multiAdder", Set(baseModule, oneAdderModule),
    Map((Name("three"), true) -> Add(Var("two"), Var("one")),
        (Name("two"), true) -> Add(Var("one"), QualifiedVar("base", "one"))))
  val multiAdderModuleEP2 = ModuleExternalPrecedence("multiAdder", Set(baseModule, oneAdderModule),
    Map((Name("three"), true) -> Add(Var("two"), Var("one")),
      (Name("two"), true) -> Add(Var("one"), QualifiedVar("base", "one"))))

  def oneRef = (baseModule.name.id, baseModule.defs.head._1._1.id)
  def twoDecl = oneAdderModule.defs.head._1._1.id
  def twoRef = (oneAdderModule.name.id, twoDecl)
  def twoInternalRef(m: Module) = m.defs.map(_._1._1).find(_.name == "two").get.id
  def twoAccess = multiAdderModuleIP2.defs.find(_._1._1.name == "three").get._2.allNames.find(_.name == "two").get.nameO

  test ("Qualified reference test") {
    val g = oneAdderModule.resolveNames()
    assert(g.V.size == 4, "There should be 4 nodes in the name graph of the oneAdderModule!")
    assert(oneAdderModule.exportedNames.size == 1, "There should be 1 exported node in the name graph of the oneAdderModule!")

    assert(g.E.size == 0, "There should be no internal edges in the name graph of the oneAdderModule!")
    assert(g.EOut.size == 2, "There should be 2 external edges in the name graph of the oneAdderModule!")
    assert(g.EOut.forall(e => e._2 == oneRef), "All external edges in the name graph of the oneAdderModule should point to 'one'!")
    assert(g.C.size == 0, "There should be no declaration conflicts in the name graph of the oneAdderModule")
  }

  test ("Qualified override for internal precedence test") {
    val g = multiAdderModuleIP.resolveNames()
    assert(g.V.size == 7, "There should be 7 nodes in the name graph of the multiAdderModuleIP!")
    assert(multiAdderModuleIP.exportedNames.size == 2, "There should be 2 exported nodes in the name graph of the multiAdderModuleIP!")

    assert(g.E.size == 0, "There should be no internal edges in the name graph of the multiAdderModuleIP!")
    assert(g.EOut.size == 4, "There should be 4 external edges in the name graph of the multiAdderModuleIP!")

    val oneRefs = g.EOut.filter(_._2 == oneRef)
    val twoRefs = g.EOut.filter(_._2 == twoRef)
    assert(oneRefs.size == 3, "There should be 3 external edges in the name graph of the multiAdderModuleIP that point to 'one'!")
    assert(twoRefs.size == 1, "There should be 1 external edges in the name graph of the multiAdderModuleIP that points to 'two'!")

    assert(g.C.size == 0, "There should be no declaration conflicts in the name graph of the multiAdderModuleIP")
  }

  test ("Qualified override for external precedence test") {
    val g = multiAdderModuleEP.resolveNames()
    assert(g.V.size == 8, "There should be 8 nodes in the name graph of the multiAdderModuleEP!")
    assert(multiAdderModuleEP.exportedNames.size == 2, "There should be 2 exported nodes in the name graph of the multiAdderModuleEP!")

    assert(g.E.size == 1, "There should be 1 internal edge in the name graph of the multiAdderModuleEP!")
    assert(g.E.head._2 == twoInternalRef(multiAdderModuleEP), "The internal edge in the name graph of the multiAdderModuleEP should point to 'two'!")
    assert(g.EOut.size == 3, "There should be 3 external edges in the name graph of the multiAdderModuleEP!")
    assert(g.EOut.forall(e => e._2 == oneRef), "All external edges in the name graph of the multiAdderModuleEP should point to 'one'!")
    assert(g.C.size == 0, "There should be no declaration conflicts in the name graph of the multiAdderModuleEP")
  }

  test ("Safely qualified reference test") {
    val g = multiAdderModuleIP2.safelyQualifiedReference(twoAccess, twoDecl).get.resolveNames()
    assert(g.V.size == 8, "There should be 8 nodes in the name graph of the multiAdderModuleEP!")
    assert(multiAdderModuleIP2.exportedNames.size == 2, "There should be 2 exported nodes in the name graph of the multiAdderModuleEP!")

    assert(g.E.size == 0, "There should be no internal edges in the name graph of the multiAdderModuleEP!")
    assert(g.EOut.size == 4, "There should be 4 external edges in the name graph of the multiAdderModuleEP!")

    val oneRefs = g.EOut.filter(_._2 == oneRef)
    val twoRefs = g.EOut.filter(_._2 == twoRef)
    assert(oneRefs.size == 3, "There should be 3 external edges in the name graph of the multiAdderModuleEP that point to 'one'!")
    assert(twoRefs.size == 1, "There should be 1 external edges in the name graph of the multiAdderModuleEP that points to 'two'!")

    assert(g.C.size == 0, "There should be no declaration conflicts in the name graph of the multiAdderModuleEP")
  }

  test ("Unnecessary safely qualified reference test") {
    val g = multiAdderModuleEP2.safelyQualifiedReference(twoAccess, twoDecl).get.resolveNames()
    assert(g.V.size == 7, "There should be 7 nodes in the name graph of the multiAdderModuleEP!")
    assert(multiAdderModuleEP2.exportedNames.size == 2, "There should be 2 exported nodes in the name graph of the multiAdderModuleEP!")

    assert(g.E.size == 0, "There should be no internal edges in the name graph of the multiAdderModuleEP!")
    assert(g.EOut.size == 4, "There should be 4 external edges in the name graph of the multiAdderModuleEP!")

    val oneRefs = g.EOut.filter(_._2 == oneRef)
    val twoRefs = g.EOut.filter(_._2 == twoRef)
    assert(oneRefs.size == 3, "There should be 3 external edges in the name graph of the multiAdderModuleEP that point to 'one'!")
    assert(twoRefs.size == 1, "There should be 1 external edges in the name graph of the multiAdderModuleEP that points to 'two'!")

    assert(g.C.size == 0, "There should be no declaration conflicts in the name graph of the multiAdderModuleEP")
  }

  test ("Safely qualified reference failure test") {
    val m = multiAdderModuleIP2.safelyQualifiedReference(twoAccess, multiAdderModuleIP.name.id)
    assert(m match {
      case None => true
      case _ => false }, "SQR creation should fail for unachievable references!")
  }
}
