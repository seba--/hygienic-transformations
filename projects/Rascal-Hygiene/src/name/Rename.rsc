module name::Rename

import name::IDs;
import name::NameGraph;

&T rename(&T t, NameGraph G1, str oldname, str newname) {
  ID declID = getID(oldname);
  if (declID in G1.E)
    declID = G1.E[declID];

  return visit (t) {
    case str decl => setID(newname, declID)
      when getID(decl) == declID
    case str ref => setID(newname, getID(ref))
      when getID(ref) in G1.E && G1.E[getID(ref)] == declID
  };
}