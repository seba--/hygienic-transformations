module lang::derric::NameRel

import lang::derric::FileFormat;
import String;
import name::IDs;
import name::NameGraph;

NameGraph resolveNames(FileFormat frm) {
  structs = collectStructs(frm);
  inh = inheritance(frm, structs);
  structs = enrichByInheritance(structs, inh);
  e = resolveInheritance(frm, structs)
    + resolveSequence(frm, structs)
    + resolveFields(frm, structs);
  v = e.use + e.def;
  return <v, e>;
}

rel[str, ID, str, ID] collectStructs(FileFormat frm)
  = { <t.name, getID(t.name), f.name, getID(f.name)> | 
       /Term t := frm, /Field f := t };

rel[str, ID, str, ID] enrichByInheritance(rel[str, ID, str, ID] structs, rel[ID,ID] inh) {
  // a struct x that inherits from struct y
  added = { <x, xId, f, fId> | <x, xId, _, _> <- structs, super <- (inh+)[xId],
            <_, super, f, fId> <- structs };
  return structs + added;
}

rel[ID, ID] inheritance(FileFormat f, rel[str, ID, str, ID] structs)
  = { <sub, sup> | /term(x, y, _) := f,
       <x, sub, _, _> <- structs, <y, sup, _, _> <- structs };
       


Edges resolveInheritance(FileFormat f, rel[str, ID, str, ID] structs) 
  = ( getID(super): decl | /term(x, super, _) := f,
       <super, ID decl, _, _> <- structs );

Edges resolveSequence(FileFormat f, rel[str, ID, str, ID] structs)
  = ( getID(x): decl | /term(x) := f.sequence,
         <x, ID decl, _, _> <- structs  );
  
  
Edges resolveFields(FileFormat frm, rel[str, ID, str, ID] structs) {
   Edges resolveField(str struct, Field f) 
     = ( getID(x): decl | /field(str x) := f, 
            <struct, _, x, ID decl> <- structs )
     + ( getID(x): decl | /field(str qualified, str x) := f, 
            <qualified, _, x, ID decl> <- structs )
     + ( getID(qualified): decl | /field(str qualified, str x) := f, 
            <qualified, ID decl, _, _> <- structs )
            
     // unfortunate duplication...
     + ( getID(x): decl | /ref(str x) := f, 
            <struct, _, x, ID decl> <- structs )
     + ( getID(x): decl | /ref(str qualified, str x) := f, 
            <qualified, _, x, ID decl> <- structs )
     + ( getID(qualified): decl | /ref(str qualified, str x) := f, 
            <qualified, ID decl, _, _> <- structs );

   return ( () | it + resolveField(t.name, f) | Term t <- frm.terms, Field f <- t.fields);
} 
  