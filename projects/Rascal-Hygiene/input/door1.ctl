events
  close  C
  open   O
  lock   L
  unlock U
end

state opened
 close => closed 
end
  
state closed
 open => opened
 lock => locked 
end 

state locked
 unlock => closed 
end
