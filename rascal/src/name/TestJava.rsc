module name::TestJava

import lang::missgrant::base::Compile;
import lang::missgrant::base::AST;
import lang::missgrant::base::Implode;
import lang::missgrant::base::NameRel;
import lang::java::NameRel;
import lang::java::jdt::m3::Core;

import name::TestString;
import name::Relation;
import name::Names;
import name::Gensym;
import util::Maybe;
import IO;
import String;
import List;

str output = "generated-missgrant";
str missGrantClass = "MissGrant";


str compile1java() = compile(missGrantClass, statemachine1());

void compile1javaToDisk() {
  writeFile(|project://<output>/src/<missGrantClass>.java|, compile1java());
}

NameGraph javaNames1() = m3toNameGraph(createM3FromEclipseProject(|project://<output>|));


Controller illCompiled1java() = load(|project://Rascal-Hygiene/input/illcompiledjava.ctl|);

str compileIllCompiled1java() = compile(missGrantClass, illCompiled1java());

void compileIllCompiled1javaToDisk() {
  writeFile(|project://<output>/src/<missGrantClass>.java|, compileIllCompiled1java());
}

NameGraph illCompiled1Names() = resolveNames(illCompiled1java());

NameGraph illCompiled1javaNames() {
  return m3toNameGraph(createM3FromEclipseProject(|project://<output>|));
}

NameGraph reconNames() = 
   insertSourceNames(illCompiled1javaNames(), 
     reconstruct(origins(compileIllCompiled1java()),
         |project://<output>/src/<missGrantClass>.java|), 
         |project://Rascal-Hygiene/input/illcompiledjava.ctl|); 

NameGraph resolveJava(lrel[Maybe[loc], str] src) {
  // Ugh, make this nice..
  writeFile(|project://<output>/src/<missGrantClass>.java|, ("" | it + x | x <- src<1> ));
  return insertSourceNames(m3toNameGraph(createM3FromEclipseProject(|project://<output>|)), 
     reconstruct(src,
         |project://<output>/src/<missGrantClass>.java|), 
         |project://Rascal-Hygiene/input/illcompiledjava.ctl|);
}

str fixIllCompiledJava1() { 
  compileIllCompiled1javaToDisk(); // start clean;
  orgs = fixHygiene2(illCompiled1Names(), origins(compileIllCompiled1java()), resolveJava);
  newSource = ( "" | it + x | x <- orgs<1> );
  writeFile(|project://<output>/src/<missGrantClass>.java|, newSource);
  return newSource;
}

void compileIllCompiled1javaToDisk() {
  writeFile(|project://<output>/src/<missGrantClass>.java|, compileIllCompiled1java());
}


lrel[Maybe[loc], str] rename(Edges refs, lrel[Maybe[loc], str] src, map[ID,str] subst) {
  src = for (<just(loc l), str x> <- src) {
    if ({l} in subst) {
      //println("Renaming <l>");
      append <just(l), subst[{l}]>;
    }
    else if ({l} in refs, def := refs[{l}], def in subst) {
      //println("Renaming definition <def>");
      append <just(l), subst[def]>;
    } 
    else {
      append <just(l), x>;
    }
  }
  return src;
}


lrel[Maybe[loc], str] fixHygiene2(<Vs,Es,Ns>, lrel[Maybe[loc], str] t, NameGraph(lrel[Maybe[loc], str]) resolveT) {
  Gt = <Vt,Et,Nt> = resolveT(t);
  
  //println("Source edges:");
  //iprintln(Es);
  //println("Target edges:");
  //iprintln(Et);
  //println("Synthesized nodes");
  //iprintln(Vt - Vs);
  
  
  notPreserveSourceBinding =    (u:Et[u] | u <- Vs & Vt, u in Es, u in Et && Es[u] != Et[u]);
  //notPreserveDefinitionScope =  (u:Et[u] | d <- Vs & Vt, u <- Et, Et[u] == d, u in Es ? Es[u] != d : true);
  notSafeDefinitionReferences = (u:Et[u] | u <- Vs & Vt, u notin Es, u in Et, Et[u] != u);
  
  //println("not preserve source binding:");
  //iprintln(notPreserveSourceBinding);
  //println("not preserve definition scope:");
  //iprintln(notPreserveDefinitionScope);
  //println("not safe definition references:");
  //iprintln(notSafeDefinitionReferences);

  allBadRefs = notPreserveSourceBinding /*+ notPreserveDefinitionScope*/ + notSafeDefinitionReferences;
  badDefinitionNodes = allBadRefs<1>;
  
  goodDefRefs = ( u:Es[u] | u <- notPreserveSourceBinding<0>, u in Es);
  // goodUseRefs required?
  //goodUseRefs = ( u:d | d <- notPreserveDefinitionScope<1>, u <- Es, Es[u] == d);
  
  //println("All bad refs:");
  //iprintln(allBadRefs);
  
  if (badDefinitionNodes == {})
    return t;

  //println("Bad definition nodes:");
  //iprintln(badDefinitionNodes);

  
  usedNames = Nt<1>;
  subst = ();
  
  for (l <- badDefinitionNodes) {
    fresh = freshName(usedNames, nameOf(l, Gt));
    usedNames += fresh;
    subst += (l : fresh);
  };
  
  Et_new = Et - allBadRefs + goodDefRefs;// + goodUseRefs;
  
  //println("New reference graph:");
  //iprintln(Et_new);
  
  t_new = rename(Et_new, t, subst);
  
  return fixHygiene2(<Vs,Es,Ns>, t_new, resolveT);
}



NameGraph insertSourceNames(NameGraph targetGraph, lrel[loc, loc, str] reconOrgs, loc orgLoc) {
  return visit (targetGraph) {
    case loc l => sourceLoc
      when <l, loc sourceLoc, _> <- reconOrgs//, sourceLoc.path == orgLoc.path
  }
}

lrel[loc, loc, str] reconstruct(lrel[Maybe[loc], str] orgs, loc src) {
  cur = |<src.scheme>://<src.authority><src.path>|(0, 0, <1, 0>, <1,0>);
 
  result = for (<org, str sub> <- orgs) {
    cur.length = size(sub);
    nls = size(findAll(sub, "\n"));
    cur.end.line += nls;
    if (nls != 0) {
      // reset
      cur.end.column = size(sub) - findLast(sub, "\n") - 1;
    }
    else {
      cur.end.column += size(sub);
    }
    if (just(loc l) := org) {
      append <cur, l, sub>;
    }
    else {
      throw "No origin";
    }
    cur.offset += size(sub);
    cur.begin.column = cur.end.column;
    cur.begin.line = cur.end.line;
  }
  
  return result;
}


