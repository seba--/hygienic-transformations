package lang.lambda.module

import lang.lambda._
import lang.lambda.num._
import name._
import org.scalatest._

class ModuleNameFixTest extends FunSuite {
  val fixer: NameFix = NameFix.fixerModular

  val baseModule = ModuleNoPrecedence("base", Set(),
    Map((Name("one"), true) -> Num(1), (Name("two"), true) -> Num(2)))
  val baseOne = baseModule.allNames.find(_.name == "one").get
  val baseTwo = baseModule.allNames.find(_.name == "two").get

  val dependentModule = ModuleInternalPrecedence("dependent", Set(baseModule),
    Map((Name("three"), true) -> Add(Var("one"), Var("two")), (Name("two"), true) -> Num(2)))
  val dependentModuleAlt = ModuleInternalPrecedence("dependent", Set(baseModule),
    Map((Name("three"), true) -> Add(Var("one"), Lam("two", Var("two"))), (Name("two"), true) -> Num(2)))

  val dependentOneRef = dependentModule.allNames.find(_.name == "one").get
  val dependentTwo = dependentModule.exportedNames.find(_.name == "two").get.id
  val dependentTwoRef = dependentModule.allNames.find(n => n.name == "two" && n != dependentTwo).get
  val dependentAltTwo = dependentModuleAlt.exportedNames.find(_.name == "two").get.id
  val dependentAltTwoDef = dependentModuleAlt.defs(("three", true)) match { case Add(_, Lam(d, _)) => d.id }
  val dependentAltTwoRef = dependentModuleAlt.defs(("three", true)) match { case Add(_, Lam(_, Var(r))) => r.id }

  // Before: Not defined, After: Bound internally to src
  val nameGraphTwoUndefined = dependentModule.resolveNames() -- NameGraphModular(Name("dependent").id, Set(dependentTwoRef), Map(dependentTwoRef -> dependentTwo), Map(), Set())
  // Before: Not defined, After: Bound externally to src
  val nameGraphOneUndefined = dependentModule.resolveNames() -- NameGraphModular(Name("dependent").id, Set(dependentOneRef), Map(), Map(dependentOneRef -> (baseModule.name.id, baseOne)), Set())

  // Before: Unbound, After: Bound internally
  val nameGraphTwoUnbound = dependentModule.resolveNames() -- NameGraphModular(Name("dependent").id, Set(), Map(dependentTwoRef -> dependentTwo), Map(), Set())
  // Before: Unbound, After: Bound externally
  val nameGraphOneUnbound = dependentModule.resolveNames() -- NameGraphModular(Name("dependent").id, Set(), Map(), Map(dependentOneRef -> (baseModule.name.id, baseOne)), Set())

  // Before: Bound internally, After: Bound internally to syn
  val nameGraphAltTwoBoundInternallyDefRemoved = dependentModuleAlt.resolveNames() --
    NameGraphModular(Name("dependent").id, Set(dependentAltTwoDef), Map(dependentAltTwoRef -> dependentAltTwoDef), Map(), Set()) ++ NameGraphModular(Name("dependent").id, Set(), Map(dependentAltTwoRef -> dependentAltTwo), Map(), Set())
  // Before: Bound internally, After: Bound internally to other src
  val nameGraphAltTwoBoundInternally = dependentModuleAlt.resolveNames() --
    NameGraphModular(Name("dependent").id, Set(), Map(dependentAltTwoRef -> dependentAltTwoDef), Map(), Set()) ++ NameGraphModular(Name("dependent").id, Set(), Map(dependentAltTwoRef -> dependentAltTwo), Map(), Set())
  // Before: Bound internally, After: Bound externally
  val lostOneDef = Name("one").id
  val nameGraphOneBoundInternally = dependentModule.resolveNames() --
    NameGraphModular(Name("dependent").id, Set(), Map(), Map(dependentOneRef -> (baseModule.name.id, baseOne)), Set()) ++ NameGraphModular(Name("dependent").id, Set(lostOneDef), Map(dependentOneRef -> lostOneDef), Map(), Set())

  // Before: Bound externally, After: Bound internally
  val nameGraphTwoBoundExternally = dependentModule.resolveNames() --
    NameGraphModular(Name("dependent").id, Set(dependentTwo), Map(dependentTwoRef -> dependentTwo), Map(), Set()) ++ NameGraphModular(Name("dependent").id, Set(), Map(), Map(dependentTwoRef -> (baseModule.name.id, baseTwo)), Set())

  test ("Not defined -> Bound internally to src test") {
    val fixed = fixer.nameFix(nameGraphTwoUndefined, dependentModule).resolveNames()
    info(fixed.toString)
    assert (!fixed.E.contains(dependentTwoRef), "Two must not be bound internally after fixing!")
    assert (!fixed.EOut.contains(dependentTwoRef), "Two must not be bound externally after fixing!")
  }

  test ("Not defined -> Bound externally to src test") {
    val fixed = fixer.nameFix(nameGraphOneUndefined, dependentModule).resolveNames()
    info(fixed.toString)
    assert (!fixed.E.contains(dependentOneRef), "One must not be bound internally after fixing!")
    assert (!fixed.EOut.contains(dependentOneRef), "One must not be bound externally after fixing!")
  }

  test ("Unbound -> Bound internally to src test") {
    val fixed = fixer.nameFix(nameGraphTwoUnbound, dependentModule).resolveNames()
    info(fixed.toString)
    assert (!fixed.E.contains(dependentTwoRef), "Two must not be bound internally after fixing!")
    assert (!fixed.EOut.contains(dependentTwoRef), "Two must not be bound externally after fixing!")
  }

  test ("Unbound -> Bound externally to src test") {
    val fixed = fixer.nameFix(nameGraphOneUnbound, dependentModule).resolveNames()
    info(fixed.toString)
    assert (!fixed.E.contains(dependentOneRef), "One must not be bound internally after fixing!")
    assert (!fixed.EOut.contains(dependentOneRef), "One must not be bound externally after fixing!")
  }

  test ("Bound internally -> Bound internally to syn") {
    val fixed = fixer.nameFix(nameGraphAltTwoBoundInternallyDefRemoved, dependentModuleAlt).resolveNames()
    info(fixed.toString)
    assert (fixed.E(dependentAltTwoRef) == dependentAltTwo, "Two must be bound as in the source graph after fixing!")
    assert (!fixed.EOut.contains(dependentAltTwoRef), "Two must not be bound externally after fixing!")
  }

  test ("Bound internally -> Bound internally to other src") {
    val fixed = fixer.nameFix(nameGraphAltTwoBoundInternally, dependentModuleAlt).resolveNames()
    info(fixed.toString)
    assert (fixed.E(dependentAltTwoRef) == dependentAltTwo, "Two must be bound as in the source graph after fixing!")
    assert (!fixed.EOut.contains(dependentAltTwoRef), "Two must not be bound externally after fixing!")

  }

  test ("Bound internally -> Bound externally") {
    val fixed = fixer.nameFix(nameGraphOneBoundInternally, dependentModule).resolveNames()
    info(fixed.toString)
    assert (!fixed.E.contains(dependentOneRef), "One must be unbound (as previous binding doesn't exist any more) after fixing!")
    assert (!fixed.EOut.contains(dependentOneRef), "One must be unbound (as previous binding doesn't exist any more) after fixing!")
  }

  test ("Bound externally -> Bound internally") {
    val fixed = fixer.nameFix(nameGraphOneBoundInternally, dependentModule).resolveNames()
    info(fixed.toString)
    assert (!fixed.E.contains(dependentTwoRef), "Two must not be bound internally after fixing!")
    assert (fixed.EOut(dependentTwoRef) == (baseModule.name.id, baseTwo), "Two must be bound as in the source graph after fixing!")
  }
}
