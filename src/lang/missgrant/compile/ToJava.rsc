module lang::missgrant::compile::ToJava

import  lang::missgrant::ast::MissGrant;

public str controller2java(Controller ctl) {
  return "public class Controller {
  	     '  public static void main(String args[]) {
  	     '  }
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
  return "public boolean <event.name>(String token) {
         '  return token.equals(\"<event.token>\");
         '}";
}

public str command2java(Command command) {
  return "public void <command.name>() {
         '  System.out.println(\"<command.token>\");
         '}";
}

public str state2java(State state) {
  return "public state_<state.name>(Scanner scanner) {
         '  <for (a <- state.actions) {>
         '  <a>();
         '  <}>
         '  String token = scanner.nextLine();
         '  <for (t <- state.transitions) {>
         '  if (<t.event>(token)) {
         '     <t.state>(scanner);
         '  }
         '  <}>
         '}";   
}

