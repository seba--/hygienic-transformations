package lang.lambda.module

import lang.lambda._
import lang.lambda.num._
import name._
import name.namefix.NameFix
import org.scalatest._

class ModuleNameFixTest extends FunSuite {
  val fixer = NameFix.fixerModular

  val baseModule = ModuleNoPrecedence("base", Set(),
    Map((Name("one"), true) -> Num(1)))
  val baseOne = baseModule.allNames.find(_.name == "one").get

  val baseModuleAlt = ModuleNoPrecedence("base", Set(),
    Map((Name("one"), true) -> Num(1), (Name("two"), true) -> Num(2)))
  val baseAltOne = baseModuleAlt.allNames.find(_.name == "one").get
  val baseAltTwo = baseModuleAlt.allNames.find(_.name == "two").get

  val dependentModule = ModuleInternalPrecedence("dependent", Set(baseModule),
    Map((Name("three"), true) -> Add(Var("one"), Var("two")), (Name("two"), true) -> Num(2)))
  val dependentModuleAlt = ModuleInternalPrecedence("dependent", Set(baseModuleAlt),
    Map((Name("three"), true) -> Add(Var("one"), Lam("two", Add(Var("two"), Var("two")))), (Name("two"), true) -> Num(2)))

  val dependentOneRef = dependentModule.allNames.find(_.name == "one").get
  val dependentTwo = dependentModule.exportedNames.find(_.name == "two").get.id
  val dependentTwoRef = dependentModule.allNames.find(n => n.name == "two" && n != dependentTwo).get
  val dependentAltTwo = dependentModuleAlt.exportedNames.find(_.name == "two").get.id
  val dependentAltTwoDef = dependentModuleAlt.defs(("three", true)) match { case Add(_, Lam(d, _)) => d.id }
  val dependentAltTwoRef1 = dependentModuleAlt.defs(("three", true)) match { case Add(_, Lam(_, Add(Var(r), _))) => r.id }
  val dependentAltTwoRef2 = dependentModuleAlt.defs(("three", true)) match { case Add(_, Lam(_, Add(_, Var(r)))) => r.id }

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
    NameGraphModular(Name("dependent").id, Set(dependentAltTwoDef, dependentAltTwoRef1), Map(dependentAltTwoRef2 -> dependentAltTwoDef), Map(), Set()) ++ NameGraphModular(Name("dependent").id, Set(), Map(dependentAltTwoRef2 -> dependentAltTwo), Map(), Set())
  // Before: Bound internally, After: Bound internally to other src
  val nameGraphAltTwoBoundInternally = dependentModuleAlt.resolveNames() --
    NameGraphModular(Name("dependent").id, Set(), Map(dependentAltTwoRef2 -> dependentAltTwoDef), Map(), Set()) ++ NameGraphModular(Name("dependent").id, Set(), Map(dependentAltTwoRef2 -> dependentAltTwo), Map(), Set())
  // Before: Bound internally, After: Bound externally
  val lostOneDef = Name("one").id
  val nameGraphOneBoundInternally = dependentModule.resolveNames() --
    NameGraphModular(Name("dependent").id, Set(), Map(), Map(dependentOneRef -> (baseModule.name.id, baseOne)), Set()) ++ NameGraphModular(Name("dependent").id, Set(lostOneDef), Map(dependentOneRef -> lostOneDef), Map(), Set())

  // Before: Bound externally, After: Bound internally
  val nameGraphTwoBoundExternally = dependentModuleAlt.resolveNames() --
    NameGraphModular(Name("dependent").id, Set(dependentAltTwo), Map(dependentAltTwoRef2 -> dependentAltTwo), Map(), Set()) ++ NameGraphModular(Name("dependent").id, Set(), Map(), Map(dependentAltTwoRef2 -> (baseModuleAlt.name.id, baseAltTwo)), Set())

  test ("Not defined -> Bound internally to src test") {
    val fixed = fixer.nameFix(Set[(NameGraphModular, Module)]((baseModule.resolveNames(), baseModule), (nameGraphTwoUndefined, dependentModule))).map(_._1)
    for (module <- fixed) {
      info(module.toString)
      val moduleGraph = module.resolveNames()
      if (moduleGraph.ID == dependentModule.name.id) {
        assert(!moduleGraph.E.contains(dependentTwoRef), "Two must not be bound internally after fixing!")
        assert(!moduleGraph.EOut.contains(dependentTwoRef), "Two must not be bound externally after fixing!")
      }
    }
  }

  test ("Not defined -> Bound externally to src test") {
    val fixed = fixer.nameFix(Set[(NameGraphModular, Module)]((baseModule.resolveNames(), baseModule), (nameGraphOneUndefined, dependentModule))).map(_._1)
    for (module <- fixed) {
      info(module.toString)
      val moduleGraph = module.resolveNames()
      if (moduleGraph.ID == dependentModule.name.id) {
        assert (!moduleGraph.E.contains(dependentOneRef), "One must not be bound internally after fixing!")
        assert (!moduleGraph.EOut.contains(dependentOneRef), "One must not be bound externally after fixing!")
      }
    }
  }

  test ("Unbound -> Bound internally to src test") {
    val fixed = fixer.nameFix(Set[(NameGraphModular, Module)]((baseModule.resolveNames(), baseModule), (nameGraphTwoUnbound, dependentModule))).map(_._1)
    for (module <- fixed) {
      info(module.toString)
      val moduleGraph = module.resolveNames()
      if (moduleGraph.ID == dependentModule.name.id) {
        assert (!moduleGraph.E.contains(dependentTwoRef), "Two must not be bound internally after fixing!")
        assert (!moduleGraph.EOut.contains(dependentTwoRef), "Two must not be bound externally after fixing!")
      }
    }
  }

  test ("Unbound -> Bound externally to src test") {
    val fixed = fixer.nameFix(Set[(NameGraphModular, Module)]((baseModule.resolveNames(), baseModule), (nameGraphOneUnbound, dependentModule))).map(_._1)
    for (module <- fixed) {
      info(module.toString)
      val moduleGraph = module.resolveNames()
      if (moduleGraph.ID == dependentModule.name.id) {
        assert (!moduleGraph.E.contains(dependentOneRef), "One must not be bound internally after fixing!")
        assert (!moduleGraph.EOut.contains(dependentOneRef), "One must not be bound externally after fixing!")
      }
    }
  }

  test ("Bound internally -> Bound internally to syn") {
    val fixed = fixer.nameFix(Set[(NameGraphModular, Module)]((baseModule.resolveNames(), baseModule), (nameGraphAltTwoBoundInternallyDefRemoved, dependentModuleAlt))).map(_._1)
    for (module <- fixed) {
      info(module.toString)
      val moduleGraph = module.resolveNames()
      if (moduleGraph.ID == dependentModuleAlt.name.id) {
        assert (moduleGraph.E(dependentAltTwoRef2) == dependentAltTwo, "Two must be bound as in the source graph after fixing!")
        assert (!moduleGraph.EOut.contains(dependentAltTwoRef2), "Two must not be bound externally after fixing!")
      }
    }
  }

  test ("Bound internally -> Bound internally to other src") {
    val fixed = fixer.nameFix(Set[(NameGraphModular, Module)]((baseModule.resolveNames(), baseModule), (nameGraphAltTwoBoundInternally, dependentModuleAlt))).map(_._1)
    for (module <- fixed) {
      info(module.toString)
      val moduleGraph = module.resolveNames()
      if (moduleGraph.ID == dependentModuleAlt.name.id) {
        assert (moduleGraph.E(dependentAltTwoRef1) == dependentAltTwoDef, "Two (left) must be bound as in the source graph after fixing!")
        assert (moduleGraph.E(dependentAltTwoRef2) == dependentAltTwo, "Two (right) must be bound as in the source graph after fixing!")
        assert (!moduleGraph.EOut.contains(dependentAltTwoRef1), "Two (left) must not be bound externally after fixing!")
        assert (!moduleGraph.EOut.contains(dependentAltTwoRef2), "Two (right) must not be bound externally after fixing!")
      }
    }
  }

  test ("Bound internally -> Bound externally") {
    val fixed = fixer.nameFix(Set[(NameGraphModular, Module)]((baseModule.resolveNames(), baseModule), (nameGraphOneBoundInternally, dependentModule))).map(_._1)
    for (module <- fixed) {
      info(module.toString)
      val moduleGraph = module.resolveNames()
      if (moduleGraph.ID == dependentModule.name.id) {
        assert (!moduleGraph.E.contains(dependentOneRef), "One must be unbound (as previous binding doesn't exist any more) after fixing!")
        assert (!moduleGraph.EOut.contains(dependentOneRef), "One must be unbound (as previous binding doesn't exist any more) after fixing!")
      }
    }
  }

  test ("Bound externally -> Bound internally") {
    val fixed = fixer.nameFix(Set[(NameGraphModular, Module)]((baseModuleAlt.resolveNames(), baseModuleAlt), (nameGraphTwoBoundExternally, dependentModuleAlt))).map(_._1)
    for (module <- fixed) {
      info(module.toString)
      val moduleGraph = module.resolveNames()
      if (moduleGraph.ID == dependentModuleAlt.name.id) {
        assert (moduleGraph.E(dependentAltTwoRef1) == dependentAltTwoDef, "Two (left) must be bound as in the source graph after fixing!")
        assert (!moduleGraph.EOut.contains(dependentAltTwoRef1), "Two (left) must not be bound externally after fixing!")
        assert (!moduleGraph.E.contains(dependentAltTwoRef2), "Two (right) must not be bound internally after fixing!")
        assert (moduleGraph.EOut(dependentAltTwoRef2) == (baseModuleAlt.name.id, baseAltTwo), "Two (right) must be bound as in the source graph after fixing!")
      }
    }
  }
}
