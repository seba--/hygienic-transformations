module lang::derric::XRef

import lang::derric::Syntax;
import ParseTree;

public start[FileFormat] xrefFormat(Tree pt) {
  // ugh, types...
  if (FileFormat f := pt.top) {
      table = ();
  
	  f.terms = visit (f.terms) {
	    case lang::derric::Syntax::Term x: {
	        table[x.name] = (x)@\loc;
	        ftable = ();
	        visit (x.fields) {
	          case lang::derric::Syntax::Field f: 
	            ftable["<f.name>"] = f@\loc;
	        }
	        x.fields = visit (x.fields) {
	          case ExpressionId eid => eid[@link=ftable["<eid>"]]
	                   when ftable["<eid>"]?
	        }
	        insert x;
	    }
	  }
	  
	  
	  f.sequence = visit (f.sequence) {
	    case Id id => id[@link=table[id]]
	       when table[id]? 
	  }
	  
	  f.terms = visit (f.terms) {
	    case Term t => t[super=t.super[@link=table[t.super]]]
	      when t has super, table[t.super]?
	  }
	  return pt[top=f];
	  
  }
  throw "Not a file format: <pt>";
}