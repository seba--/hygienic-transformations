module name::attic::Utils

import String;
import Node;
import IO;

node annotateWithOrgs(node t) {
  int n = 1;
  set[str] smaller(set[loc] locs) {
    ls = {};
    for (l <- locs) {
      println(l);
      str l2;
      if (l.fragment == "") {
        l2 = "$input$";
      }
      else {
        l2 = "$compiler$#<n>";
        n += 1;
      }
      l2 += "(<l.offset>,<l.length>)";
      ls += {l2};
    }
    return ls;
  }

  return visit (delAnnotationsRec(t)) {
    case value v => <x, smaller(originsOnly(x))>
       when str x := v
  }
} 

