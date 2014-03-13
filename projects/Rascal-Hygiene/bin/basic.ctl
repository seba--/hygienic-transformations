events
 doorClosed D1CL
 lightOn    L1ON
end 

state idle
 doorClosed => active 
end
  
state active
 lightOn => idle 
end

