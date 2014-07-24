module name::tests::TestDerric


import lang::java::NameRel;
import lang::java::jdt::m3::Core;
import lang::derric::NameRel;
import lang::derric::FileFormat;
import lang::derric::Syntax;
import lang::derric::BuildFileFormat;
import lang::derric::DesugarFileFormat;
import lang::derric::CheckFileFormat;
import lang::derric::PropagateDefaultsFileFormat;
import lang::derric::PropagateConstantsFileFormat;
import lang::derric::AnnotateFileFormat;
import lang::derric::GenerateDerric;
import lang::derric::Validator;
import lang::derric::BuildValidator;
import lang::derric::GenerateJava;
import lang::derric::GenerateFactoryJava;

import name::NameGraph;
import name::NameFixString;
import name::IDs;
import name::Gensym;
import name::HygienicCorrectness;
import util::Maybe;
import ParseTree;
import IO;
import String;
import List;

/*
 * NOTE: create an empty java project generated-derric in your Eclipse workspace.
 * Add the Rascal-Hygiene project as a dependency to the build path.
 * (This is needed (as of now) to get name analysis from JDT.)
 */

str output = "generated-derric";

str javaPackageName = "org.derric_lang.validator.generated";
str javaPathPrefix = "/" + replaceAll(javaPackageName, ".", "/") + "/";
str javaClassSuffix = "Validator";
str javaFileSuffix = ".java";
str outputProject = "generated-derric";

FileFormat jpegAST() = myLoad(|project://Rascal-Hygiene/formats/jpeg.derric|);
NameGraph jpegNames() = resolveNames(jpegAST());
str jpegCompiled() = compile(jpegAST()); 

void writeCompiled(FileFormat f) {
  src = compile(f);
  orgs = origins(src);
  classPrefix = f.name;
  writeFile(|project://<outputProject>/src<javaPathPrefix><classPrefix>Validator.java|, src);
  writeFile(|project://<outputProject>/src<javaPathPrefix><classPrefix>Validator.origins|, orgs);
}

void writeJPEGCompiled() {
  writeCompiled(jpegAST());
}


FileFormat pngAST() = myLoad(|project://Rascal-Hygiene/formats/png.derric|);
NameGraph pngNames() = resolveNames(pngAST());
str pngCompiled() = compile(pngAST()); 

void writePNGCompiled() {
  writeCompiled(pngAST());
}

FileFormat gifAST() = myLoad(|project://Rascal-Hygiene/formats/gif.derric|);
NameGraph gifNames() = resolveNames(gifAST());
str gifCompiled() = compile(gifAST()); 

void writeGIFCompiled() {
  writeCompiled(gifAST());
}


FileFormat minbadAST() = myLoad(|project://Rascal-Hygiene/formats/minbad.derric|);
NameGraph minbadNames() = resolveNames(minbadAST());
str minbadCompiled() = compile(minbadAST()); 

void writeMinbadCompiled() {
  writeCompiled(minbadAST());
}


FileFormat badAST() = myLoad(|project://Rascal-Hygiene/formats/bad.derric|);
NameGraph badNames() = resolveNames(badAST());
str badCompiled() = compile(badAST()); 

void writeBADCompiled() {
  writeCompiled(badAST());
}


str compile(FileFormat f) {
  f = preprocess(f); // NB: don't inline.
  return generate(f.sequence, f.extensions[0], build(f), javaPackageName);
}

FileFormat preprocess(FileFormat f)
  = annotate(propagateConstants(desugar(propagateDefaults(f))));

FileFormat myLoad(loc path) = build(parse(#start[FileFormat], path).top);



NameGraph resolveJava(lrel[Maybe[loc], str] src, str class, loc dsl) {
  // Ugh, make this nice..
  writeFile(|project://<output>/src<javaPathPrefix><class>.java|, ("" | it + x | x <- src<1> ));
  return insertSourceNames(m3toNameGraph(createM3FromEclipseProject(|project://<output>|)), 
     reconstruct(src,
         |project://<output>/src<javaPathPrefix><class>.java|), 
         dsl);
}

lrel[Maybe[loc], str] fixMinBad() { 
  writeMinbadCompiled(); // start clean;
  str class = "MinbadValidator";
  NameGraph resolve(lrel[Maybe[loc], str] src) {
    return resolveJava(src, class, |project://Rascal-Hygiene/formats/minbad.derric|); 
  }
  outFile = |project://<output>/src<javaPathPrefix><class>.java|;
  orgs = nameFixString(minbadNames(), origins(minbadCompiled()), resolve, outFile);
  newSource = ( "" | it + x | x <- orgs<1> );
  writeFile(outFile, newSource);
  return orgs;
}

test bool testMinBad() {
  newSource = fixMinBad();
  return isHygienic(minbadNames(), 
     resolveJava(newSource, "MinbadValidator", |project://Rascal-Hygiene/formats/minbad.derric|));
}



