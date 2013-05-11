module lang::missgrant::retries::AST

extend lang::missgrant::base::AST;

data Transition 
  = transition(str event, str state, int number, str then)
  ;
  

