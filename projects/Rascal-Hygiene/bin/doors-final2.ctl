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
 lock => locked_final 
end 

state locked_final
 unlock => closed 
end
