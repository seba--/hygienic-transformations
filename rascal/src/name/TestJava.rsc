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

NameGraph illCompiled1Names() = resolveNames(illCompiled1java());

NameGraph illCompiled1javaNames() = m3toNameGraph(createM3FromEclipseProject(|project://<output>|));

NameGraph reconNames() = 
   insertSourceNames(illCompiled1javaNames(), 
     reconstruct(origins(compileIllCompiled1java()),
         |project://<output>/src/<missGrantClass>.java|)); 

void compileIllCompiled1javaToDisk() {
  writeFile(|project://<output>/src/<missGrantClass>.java|, compileIllCompiled1java());
}

NameGraph insertSourceNames(NameGraph targetGraph, lrel[loc, loc, str] reconOrgs) {
  return visit (targetGraph) {
    case loc l => sourceLoc
      when <l, loc sourceLoc, _> <- reconOrgs
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


