module lang::missgrant::retries::Syntax

extend lang::missgrant::base::Syntax;

syntax Transition 
  = transition: Id event "after" Nat number "=\>" Id then;

lexical Nat = [1-9][0-9]* !>> [0-9];

keyword Reserved = ... | "after";