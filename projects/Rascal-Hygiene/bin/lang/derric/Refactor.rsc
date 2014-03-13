module lang::derric::Refactor

import lang::derric::Syntax;
import ParseTree;

public start[FileFormat] rename(start[FileFormat] pt, loc oldLoc, str newName) {
  try {
    Id new = parse(#Id, newName);
    if (treeFound(Id old) := treeAt(#Id, oldLoc, pt)) {
      pt = visit (pt) {
        case Structure h: {
          if (h.name == old) {
            h.name = new;
          }
          if (h has super, h.super == old) {
            h.super = new;
          }
          insert h; 
        }
      }
      pt.top.seq = visit (pt.top.seq) {
        case old => new
      }
    }
    else {
      alert("Select an identifier first");
    }
  }
  catch _:
    alert("Not a valid new name");
  return pt; 
}
