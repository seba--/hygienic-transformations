events end

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
