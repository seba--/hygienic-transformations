module lang::missgrant::compile::ToMethods

import  lang::missgrant::ast::MissGrant;

public str controller2methods(str name, Controller ctl) {
  return "public class <name> {
  	     '  <for (e <- ctl.events) {>
  	     '  <event2java(e)>
  	     '  <}>
  	     '  <for (c <- ctl.commands) {>
  	     '  <command2java(c)>
  	     '  <}>
  	     '  <for (s <- ctl.states) {>
  	     '  <state2java(s)>
  	     '  <}>
  	     '}";
}

public str event2java(Event event) {
  return "private boolean <event.name>(String token) {
         '  return token.equals(\"<event.token>\");
         '}";
}

public str command2java(Command command) {
  return "private void <command.name>(Writer output) {
         '  output.write(\"<command.token>\");
         '}";
}

public str state2java(State state) {
  return "private void <stateName(state)>(Scanner input, Writer output) {
         '  <for (a <- state.actions) {>
         '  <a>(output);
         '  <}>
         '  String token = input.nextLine();
         '  <for (t <- state.transitions) {>
         '  if (<t.event>(token)) {
         '     <stateName(t.state)>(input, output);
         '     return;
         '  }
         '  <}>
         '}";   
}

public str stateName(State s) {
  return stateName(s.name);
}

public str stateName(str s) {
  return "state$<s>";
}
