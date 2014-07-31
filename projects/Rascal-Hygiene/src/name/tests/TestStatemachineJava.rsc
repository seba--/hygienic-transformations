module name::tests::TestStatemachineJava

import lang::missgrant::base::Compile;
import lang::missgrant::base::AST;
import lang::missgrant::base::Implode;
import lang::missgrant::base::NameRel;
import lang::java::NameRel;
import lang::java::jdt::m3::Core;

import name::NameGraph;
import name::NameFixString;
import name::IDs;
import name::Gensym;
import name::HygienicCorrectness;
import util::Maybe;
import IO;
import String;
import List;

/*
 * NOTE: create an empty java project generated-missgrant in your Eclipse workspace.
 * (This is needed (as of now) to get name analysis from JDT.)
 */

str missGrantOutput = "generated-missgrant";
str missGrantClass = "MissGrant";

Controller statemachine1() = 
  lang::missgrant::base::Implode::load(|project://Rascal-Hygiene/input/door1.ctl|);


str compile1java() = compile(missGrantClass, statemachine1());

void compile1javaToDisk() {
  writeFile(|project://<missGrantOutput>/src/<missGrantClass>.java|, compile1java());
}

NameGraph javaNames1() = m3toNameGraph(createM3FromEclipseProject(|project://<missGrantOutput>|));


Controller illCompiled1java() = 
  lang::missgrant::base::Implode::load(|project://Rascal-Hygiene/input/doors1-java-ill.ctl|);

str compileIllCompiled1java() = compile(missGrantClass, illCompiled1java());

void compileIllCompiled1javaToDisk() {
  writeFile(|project://<missGrantOutput>/src/<missGrantClass>.java|, compileIllCompiled1java());
}

NameGraph illCompiled1Names() = resolveNames(illCompiled1java());

NameGraph illCompiled1javaNames() {
  return m3toNameGraph(createM3FromEclipseProject(|project://<missGrantOutput>|));
}

NameGraph reconNames() = 
   insertSourceNames(illCompiled1javaNames(), 
     reconstruct(origins(compileIllCompiled1java()),
         |project://<missGrantOutput>/src/<missGrantClass>.java|), 
     |project://Rascal-Hygiene/input/illcompiledjava.ctl|); 

NameGraph resolveJava(lrel[Maybe[loc], str] src) {
  writeFile(|project://<missGrantOutput>/src/<missGrantClass>.java|, ("" | it + x | x <- src<1> ));
  return insertSourceNames(m3toNameGraph(createM3FromEclipseProject(|project://<missGrantOutput>|)), 
     reconstruct(src,
         |project://<missGrantOutput>/src/<missGrantClass>.java|), 
     |project://Rascal-Hygiene/input/illcompiledjava.ctl|);
}

lrel[Maybe[loc], str] fixIllCompiledJava1() {
  sm = illCompiled1java();
  smnames = resolveNames(sm);
  
  outFile = |project://<missGrantOutput>/src/<missGrantClass>.java|;
  out = compile(missGrantClass, sm);
  writeFile(outFile, out);
  
  orgs = nameFixString(smnames, origins(out), resolveJava, outFile);
  newSource = ( "" | it + x | x <- orgs<1> );
  writeFile(outFile, newSource);
  return orgs;
}

test bool testIllCompiled1() {
  newSource = fixIllCompiledJava1();
  return isHygienic(illCompiled1Names(), resolveJava(newSource));
}

void compileIllCompiled1javaToDisk() {
  writeFile(|project://<missGrantOutput>/src/<missGrantClass>.java|, compileIllCompiled1java());
}

void compileIllCompiled1javaToDiskFixed() {
  fixIllCompiledJava1();
}
