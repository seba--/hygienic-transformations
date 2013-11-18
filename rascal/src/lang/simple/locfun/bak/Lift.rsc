module lang::simple::locfun::Lift


import lang::simple::AST;
import lang::simple::locfunlocfun::AST;
import lang::simple::locfun::Decorate;
import lang::simple::locfun::Abstract;
import lang::simple::locfun::MendCall;
import Node;


Prog lift(prog(FDefs fdefs, list[Exp] main)) {
  fdefs1 = [abstractFDef(decorateFDef(fdef)) | FDef fdef <- fdefs];
  main1 = [abstractExp(decorateExp(exp)) | Exp exp <- main];
  lfs = ([] | it + fdef @ lfs | FDef fdef <- fdefs1) + ([] | it + exp @ lfs | Exp exp <- main1);
  return delAnnotationsRec(prog(fdefs1 + lfs, main1));
}

