// a comment

events
 doorClosed D1CL
 lightOn L1ON
end 

resetEvents 
end 

commands
end

state token
 doorClosed => current 
end
  
state current 
 lightOn => token 
end  


