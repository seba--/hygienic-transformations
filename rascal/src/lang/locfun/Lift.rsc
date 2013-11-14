module lang::locfun::Lift


import lang::simple::AST;
import lang::locfun::AST;
import lang::locfun::Decorate;
import lang::locfun::Abstract;
import lang::locfun::MendCall;
import Node;


Prog lift(prog(FDefs fdefs, list[Exp] main)) {
  fdefs1 = [abstractFDef(decorateFDef(fdef)) | FDef fdef <- fdefs];
  main1 = [abstractExp(decorateExp(exp)) | Exp exp <- main];
  lfs = ([] | it + fdef @ lfs | FDef fdef <- fdefs1) + ([] | it + exp @ lfs | Exp exp <- main1);
  return delAnnotationsRec(prog(fdefs1 + lfs, main1));
}

