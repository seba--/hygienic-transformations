events end

state opened
 close => closed 
end
  
state closed
 open => opened
 lock => closed-dispatch 
end 

state closed-dispatch
 unlock => closed 
end
