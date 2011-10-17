module lang::missgrant::utils::Implode

import lang::missgrant::utils::Parse;
import lang::missgrant::ast::MissGrant;
import lang::missgrant::syntax::MissGrant;

import ParseTree;
import Node;

public lang::missgrant::ast::MissGrant::Controller implode(lang::missgrant::syntax::MissGrant::Controller pt) =
  implode(#lang::missgrant::ast::MissGrant::Controller, pt);

public lang::missgrant::ast::MissGrant::Controller parseAndImplode(str src, loc org) =
  implode(parse(src, org));

public lang::missgrant::ast::MissGrant::Controller parseAndImplode(loc file) =
  implode(parse(file));
