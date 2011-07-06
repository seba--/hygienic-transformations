module lang::missgrant::ide::Rename

import lang::missgrant::syntax::MissGrant;
import util::Prompt;

import ParseTree;


public str renameEvent(Controller ctl, loc sel) {
  newName = prompt("Enter new name: ");
  newId = parse(#Id, newName);
  
  if (/Event e <- ctl, e.name == newId) {
    alert("Event <newName> already exists");
  } 
  else if (treeFound(Event t) := treeAt(#Event, sel, ctl)) {
    oldName = "<t.name>";
    ctl = visit (ctl) {
      case Transition t: {
        if (oldName == "<t.event>") {
          t.event = newId;
        }
        insert t;
      }
      case Event e: {
        if (oldName == "<e.name>") {
          e.name = newId;
        }
        insert e;
      }
      case ResetEvents rs => visit (rs) {
        case Id i => newId when oldName == "<i>" 
      }
    }
  }
  else {
    alert("No event selected");
  }
  
  return "<ctl>";
}

public str renameState(Controller ctl, loc sel) {
  newName = prompt("Enter new name: ");
  newId = parse(#Id, newName);
  
  if (/State s <- ctl, s.name == newId) {
    alert("State <newName> already exists");
  } 
  else if (treeFound(State t) := treeAt(#State, sel, ctl)) {
    oldName = "<t.name>";
    ctl = visit (ctl) {
      case Transition t: {
        if (oldName == "<t.state>") {
          t.state = newId;
        }
        insert t;
      }
      case State s: {
        if (oldName == "<s.name>") {
          s.name = newId;
        }
        insert s;
      }
    }
  }
  else {
    alert("No state selected");
  }
  
  return "<ctl>";
}