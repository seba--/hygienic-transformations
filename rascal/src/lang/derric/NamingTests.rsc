module lang::derric::NamingTests

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
import ParseTree;
import String;
import IO;

import lang::java::jdt::m3::Core;

str javaPackageName = "org.derric_lang.validator.generated";
str javaPathPrefix = "/" + replaceAll(javaPackageName, ".", "/") + "/";
str javaClassSuffix = "Validator";
str javaFileSuffix = ".java";
str outputProject = "generated-derric";

FileFormat jpegAST() = myLoad(|project://derric/formats/jpeg.derric|);
NameGraph jpegNames() = resolveNames(jpegAST());
str jpegCompiled() = compile(jpegAST()); 

void writeCompiled(FileFormat f) {
  src = compile(f);
  orgs = origins(src);
  classPrefix = toUpperCase(f.name);
  writeFile(|project://<outputProject>/src<javaPathPrefix><classPrefix>Validator.java|, src);
  writeFile(|project://<outputProject>/src<javaPathPrefix><classPrefix>Validator.origins|, orgs);
}

void writeJPEGCompiled() {
  writeCompiled(jpegAST());
}


FileFormat pngAST() = myLoad(|project://derric/formats/png.derric|);
NameGraph pngNames() = resolveNames(pngAST());
str pngCompiled() = compile(pngAST()); 

void writePNGCompiled() {
  writeCompiled(pngAST());
}

FileFormat gifAST() = myLoad(|project://derric/formats/gif.derric|);
NameGraph gifNames() = resolveNames(gifAST());
str gifCompiled() = compile(gifAST()); 

void writeGIFCompiled() {
  writeCompiled(gifAST());
}


FileFormat minbadAST() = myLoad(|project://derric/formats/minbad.derric|);
NameGraph minbadNames() = resolveNames(minbadAST());
str minbadCompiled() = compile(minbadAST()); 

void writeMinbadCompiled() {
  writeCompiled(minbadAST());
}


FileFormat badAST() = myLoad(|project://derric/formats/bad.derric|);
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


NameGraph m3toNameGraph(M3 m) {
  Edges e = ( {x}: {y} | <x, y> <- m@uses o m@declarations );
  
  map[ID, str] n = ( m@declarations[x]: n | <str n, loc x>  <- m@names );
  
  set[ID] v = { {x} | x <- m@uses<0> + m@declarations<1> };
  
  return <v, e, n>;
  
}
