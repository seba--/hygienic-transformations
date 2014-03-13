module lang::missgrant::base::Rename

import lang::missgrant::base::Syntax;
import util::Prompt;

import ParseTree;

str rename(Tree ctl, loc sel) {
  newName = prompt("Enter new name: ");
  newId = parse(#Id, newName);
  if (treeFound(Event e) := treeAt(#Event, sel, ctl)) {
    ctl = renameEvent(ctl, e.name, newId);
  }
  else if (treeFound(State s) := treeAt(#State, sel, ctl)) {
    ctl = renameState(ctl, s.name, newId);
  }
  else if (treeFound(Command c) := treeAt(#Command, sel, ctl)) {
    ctl = renameCommand(ctl, c.name, newId);
  }
  else {
    alert("Select a state, event or command to rename");
  }
  return "<ctl>";
}


private bool existing(Tree ctl, str label, type[&T] t, Id name) {
  visit (ctl) {
    case &T t: 
      if (t.name == name) {
        alert("<label> <name> already exists");
        return true;
      }
  }
  return false;
}

private Tree renameEvent(Tree ctl, Id oldName, Id newName) {
  if (existing(ctl, "Event", #Event, newName)) 
    return ctl;
  
  return visit (ctl) {
    case Transition t => t[event=newName] when t.event == oldName
    case Event e => e[name=newName] when e.name == oldName
    case ResetEvents rs => visit (rs) {
      case Id i => newName when oldName == i 
    }
  }
}

private Tree renameCommand(Tree ctl, Id oldName, Id newName) {
  if (existing(ctl, "Command", #Command, newName)) 
    return ctl;
    
  return visit (ctl) {
    case Command c => c[name=newName] when c.name == oldName
    case State s => visit (s) {
       case Actions a => visit (a) { 
          case Id i => newName when oldName == i
       }
    }
  }
}

private Tree renameState(Tree ctl, Id oldName, Id newName) {
  if (existing(ctl, "State", #State, newName)) 
    return ctl;

  return visit (ctl) {
    case Transition t => t[state=newName] when t.state == oldName
    case State s => s[name=newName] when s.name == oldName 
  }
}

