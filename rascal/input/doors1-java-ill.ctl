events
 close CLS
  open OPE
  lock LCK
  unlock ULK
end

state current
 close => closed 
end
  
state closed
 open => current
 lock => token 
end 

state token
 unlock => closed 
end
