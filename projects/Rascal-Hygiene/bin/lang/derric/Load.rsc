module lang::derric::Load

import lang::derric::Syntax;
import lang::derric::FileFormat;

import ParseTree;

start[FileFormat] parse(loc l) = parse(#start[FileFormat], l);

lang::derric::FileFormat::FileFormat 
  load(loc l) = implode(#lang::derric::FileFormat::FileFormat, 
     parse(l));