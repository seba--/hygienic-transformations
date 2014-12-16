package lang.lambda.module

import lang.lambda._
import lang.lambda.num._
import name._
import org.scalatest._

class ModuleTest extends FunSuite {
  val fixer: NameFix = NameFix.fixerModular

  val baseModule = ModuleNoPrecedence("base", Set(),
    Map((Name("one"), true) -> Num(1)))
  val oneAdderModule = ModuleNoPrecedence("oneAdder", Set(baseModule),
    Map((Name("two"), true) -> Add(Var("one"), Var("one"))))
  val multiAdderModuleIP = ModuleInternalPrecedence("multiAdder", Set(baseModule, oneAdderModule),
    Map((Name("three"), true) -> Add(Var("two"), Var("one")),
        (Name("two"), true) -> Add(Var("one"), Var("one"))))
  val multiAdderModuleEP = ModuleExternalPrecedence("multiAdder", Set(baseModule, oneAdderModule),
    Map((Name("three"), true) -> Add(Var("two"), Var("one")),
        (Name("two"), true) -> Add(Var("one"), Var("one"))))
  val multiAdderModuleNP = ModuleNoPrecedence("multiAdder", Set(baseModule, oneAdderModule),
    Map((Name("three"), true) -> Add(Var("two"), Var("one")),
        (Name("two"), true) -> Add(Var("one"), Var("one"))))

  val oneAdderModuleConflicting = ModuleNoPrecedence("oneAdder2", Set(),
    Map((Name("two"), true) -> Add(Num(1), Num(1))))
  val multiAdderModuleIPconflict = ModuleInternalPrecedence("multiAdder", Set(baseModule, oneAdderModuleConflicting, oneAdderModule),
    Map((Name("three"), true) -> Add(Var("two"), Var("one")),
        (Name("two"), true) -> Add(Var("one"), Var("one"))))
  val multiAdderModuleNPconflict = ModuleNoPrecedence("multiAdder", Set(baseModule, oneAdderModuleConflicting, oneAdderModule),
    Map((Name("three"), true) -> Add(Var("two"), Var("one")),
        (Name("two"), true) -> Add(Var("one"), Var("one"))))

  def oneRef = (baseModule.name.id, baseModule.defs.head._1._1.id)
  def twoRef = (oneAdderModule.name.id, oneAdderModule.defs.head._1._1.id)
  def twoConflictingRef = (oneAdderModuleConflicting.name.id, oneAdderModuleConflicting.defs.head._1._1.id)
  def twoInternalRef(m: Module) = m.defs.map(_._1._1).find(_.name == "two").get.id

  test ("Internal precedence test") {
    val g = multiAdderModuleIP.resolveNames()
    assert(g.V.size == 6, "There should be 6 nodes in the name graph of the multiAdderModuleIP!")
    assert(multiAdderModuleIP.exportedNames.size == 2, "There should be 2 exported nodes in the name graph of the multiAdderModuleIP!")

    assert(g.E.size == 1, "There should be 1 internal edge in the name graph of the multiAdderModuleIP!")
    assert(g.E.head._2 == twoInternalRef(multiAdderModuleIP), "The internal edge in the name graph of the multiAdderModuleIP should point to 'two'!")
    assert(g.EOut.size == 3, "There should be 3 external edges in the name graph of the multiAdderModuleIP!")
    assert(g.EOut.forall(_._2 == oneRef), "All external edges in the name graph of the multiAdderModuleIP should point to 'one'!")
    assert(g.C.size == 0, "There should be no declaration conflicts in the name graph of the multiAdderModuleIP")
  }

  test ("External precedence test") {
    val g = multiAdderModuleEP.resolveNames()
    assert(g.V.size == 6, "There should be 6 nodes in the name graph of the multiAdderModuleEP!")
    assert(multiAdderModuleEP.exportedNames.size == 2, "There should be 2 exported nodes in the name graph of the multiAdderModuleEP!")

    assert(g.E.size == 0, "There should be no internal edges in the name graph of the multiAdderModuleEP!")
    assert(g.EOut.size == 4, "There should be 4 external edges in the name graph of the multiAdderModuleEP!")

    val oneRefs = g.EOut.filter(_._2 == oneRef)
    val twoRefs = g.EOut.filter(_._2 == twoRef)
    assert(oneRefs.size == 3, "There should be 3 external edges in the name graph of the multiAdderModuleEP that point to 'one'!")
    assert(twoRefs.size == 1, "There should be 1 external edges in the name graph of the multiAdderModuleEP that points to 'two'!")

    assert(g.C.size == 0, "There should be no declaration conflicts in the name graph of the multiAdderModuleEP")
  }

  test ("Dependency renaming test (bindings)") {
    val g = multiAdderModuleEP.resolveNames(Map(twoRef -> "two0"))
    assert(g.V.size == 6, "There should be 6 nodes in the dependency renamed name graph of the multiAdderModuleEP!")
    assert(g.E.size == 1, "There should be 1 internal edge in the dependency renamed name graph of the multiAdderModuleEP!")
    assert(g.E.head._2 == twoInternalRef(multiAdderModuleEP), "The internal edge in the dependency renamed name graph of the multiAdderModuleEP should point to 'two'!")
    assert(g.EOut.size == 3, "There should be 3 external edges in the dependency renamed name graph of the multiAdderModuleEP!")
    assert(g.EOut.forall(_._2 == oneRef), "All external edges in the dependency renamed name graph of the multiAdderModuleEP should point to 'one'!")
    assert(g.C.size == 0, "There should be no declaration conflicts in the dependency renamed name graph of the multiAdderModuleEP")

    val g2 = multiAdderModuleEP.resolveNames(Map(twoRef -> "two0", oneRef -> "two"))
    assert(g2.V.size == 6, "There should be 6 nodes in the dependency renamed name graph of the multiAdderModuleEP!")
    assert(g2.E.size == 0, "There should be no internal edges in the dependency renamed name graph of the multiAdderModuleEP!")
    assert(g2.EOut.size == 1, "There should be 1 external edge in the dependency renamed name graph of the multiAdderModuleEP!")
    assert(g2.EOut.head._2 == oneRef, "There should be one external edge in the dependency renamed name graph of the multiAdderModuleEP that points to 'one'!")
    assert(g2.C.size == 0, "There should be no declaration conflicts in the dependency renamed name graph of the multiAdderModuleEP")
  }

  test ("Dependency renaming test (conflicts)") {
    val g = multiAdderModuleEP.resolveNames(Map(oneRef -> "two"))
    assert(g.V.size == 6, "There should be 6 nodes in the dependency renamed name graph of the multiAdderModuleEP!")
    assert(g.E.size == 0, "There should be no internal edges in the dependency renamed name graph of the multiAdderModuleEP!")
    assert(g.EOut.size == 1, "There should be one external edge in the dependency renamed name graph of the multiAdderModuleEP!")
    assert(g.C.size == 1, "There should be 1 declaration conflict in the dependency renamed name graph of the multiAdderModuleEP")
    val conflict = Set(oneRef._2, twoRef._2)
    assert(g.C.head == conflict, "The declaration conflict in the dependency renamed name graph of the multiAdderModuleEP should be between the external declarations of 'two'")
  }

  test ("No precedence test") {
    val g = multiAdderModuleNP.resolveNames()
    assert(g.V.size == 6, "There should be 6 nodes in the name graph of the multiAdderModuleNP!")
    assert(multiAdderModuleNP.exportedNames.size == 2, "There should be 2 exported nodes in the name graph of the multiAdderModuleNP!")

    assert(g.E.size + g.EOut.size == 4, "There should be a total number of 4 edges in the name graph of the multiAdderModuleNP!")

    val oneRefs = g.EOut.filter(_._2 == oneRef)
    val twoRefs = g.EOut.filter(_._2 == twoRef)
    val twoRefsInternal = g.E.filter(_._2 == twoInternalRef(multiAdderModuleNP))
    assert(oneRefs.size == 3, "There should be 3 external edges in the name graph of the multiAdderModuleNP that point to 'one'!")
    assert(twoRefs.size + twoRefsInternal.size == 1, "There should be 1 edge in the name graph of the multiAdderModuleNP that points to either the internal or external 'two'!")

    assert(g.C.size == 1, "There should be 1 declaration conflict in the name graph of the multiAdderModuleNP")

    val conflict = Set(twoRef._2, twoInternalRef(multiAdderModuleNP))
    assert(g.C.head == conflict, "The declaration conflict in the name graph of the multiAdderModuleNP should be between the internal and the external declaration of 'two'")
  }

  test ("Internal precedence import conflict test") {
    val g = multiAdderModuleIPconflict.resolveNames()
    assert(g.V.size == 6, "There should be 6 nodes in the name graph of the multiAdderModuleIPconflict!")
    assert(multiAdderModuleIPconflict.exportedNames.size == 2, "There should be 2 exported nodes in the name graph of the multiAdderModuleIPconflict!")

    assert(g.E.size == 1, "There should be 1 internal edge in the name graph of the multiAdderModuleIPconflict!")
    assert(g.E.head._2 == twoInternalRef(multiAdderModuleIPconflict), "The internal edge in the name graph of the multiAdderModuleIPconflict should point to 'two'!")
    assert(g.EOut.size == 3, "There should be 3 external edges in the name graph of the multiAdderModuleIPconflict!")
    assert(g.EOut.forall(_._2 == oneRef), "All external edges in the name graph of the multiAdderModuleIPconflict should point to 'one'!")
    assert(g.C.size == 1, "There should be an declaration conflict in the name graph of the multiAdderModuleIPconflict")

    val conflict = Set(twoRef._2, twoConflictingRef._2)
    assert(g.C.head == conflict, "The declaration conflict in the name graph of the multiAdderModuleIPconflict should be between the declarations of 'two' in the oneAdderModules")
  }

  test ("No precedence import conflict test") {
    val g = multiAdderModuleNPconflict.resolveNames()
    assert(g.V.size == 6, "There should be 6 nodes in the name graph of the multiAdderModuleNPconflict!")
    assert(multiAdderModuleNPconflict.exportedNames.size == 2, "There should be 2 exported nodes in the name graph of the multiAdderModuleNPconflict!")

    assert(g.E.size + g.EOut.size == 4, "There should be a total number of 4 edges in the name graph of the multiAdderModuleNPconflict!")

    val oneRefs = g.EOut.filter(_._2 == oneRef)
    val twoRefs = g.EOut.filter(_._2 == twoRef)
    val twoRefsInternal = g.E.filter(_._2 == twoInternalRef(multiAdderModuleNPconflict))
    assert(oneRefs.size == 3, "There should be 3 external edges in the name graph of the multiAdderModuleNPconflict that point to 'one'!")
    assert(twoRefs.size + twoRefsInternal.size == 1, "There should be 1 edge in the name graph of the multiAdderModuleNPconflict that points to either the internal or external 'two'!")

    assert(g.C.size == 1, "There should be 1 declaration conflict in the name graph of the multiAdderModuleNPconflict")

    val declConflict = Set(twoRef._2, twoInternalRef(multiAdderModuleNPconflict), twoConflictingRef._2)
    assert(g.C.head == declConflict, "The declaration conflict in the name graph of the multiAdderModuleNPconflict should be between the internal and both external declarations of 'two'")
  }
}
