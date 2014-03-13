module lang::derric::NormalizeAST

import lang::derric::FileFormat;
import String;
import List;
import IO;

/*
 * this module normalizes imploded ASTs to ASTs conforming
 * the structure expected by the back-end.
 */

FileFormat normalize(FileFormat f) {
  return visit (f) {
    // NB: type is needed here!! field(str n) is also in Specification
    case Field field(str n): insert field(n, [], [], noValue())[@location=x@location];
    
    case x:field(n, list[FieldModifier] fms) =>
        field(n, modifiers(fms), qualifiers(fms), content(fms)[0])[@location=x@location]
      when content(fms) != []

    case x:field(n, list[FieldModifier] fms) =>
        field(n, modifiers(fms), qualifiers(fms), expressions(fms)[0])[@location=x@location]
      when size(expressions(fms)) == 1

    case x:field(n, list[FieldModifier] fms) =>
        field(n, modifiers(fms), qualifiers(fms), expressions(fms))[@location=x@location]

    case Field x => field(x.name, [], [], noValue())[@location=x@location]
      when !(x has fmodifiers)

    case Field x: {println("Not normalized: "); iprintln(x); }
        
    case Specification x:number(s) => const(makeInt(s))[@location=x@location]
    case Specification x:string(s) => const(makeString(s))[@location=x@location]
    
    case Expression x:number(s) => \value(makeInt(s))[@location=x@location]
    case Expression x:string(s) => \value(makeString(s))[@location=x@location]

	case x:terminatedBefore() => terminator(false)[@location=x@location]
	case x:terminatedBy() => terminator(true)[@location=x@location]
	
     
  }
}


int makeInt(str s) {
	if (startsWith(s, "0x") || startsWith(s, "0X")) return toInt(substring(s, 2), 16); 
	else if (startsWith(s, "0o") || startsWith(s, "0O")) return toInt(substring(s, 2), 8); 
	else if (startsWith(s, "0b") || startsWith(s, "0B")) return toInt(substring(s, 2), 2);
	else return toInt(s); 
}

str makeString(str s) = substring(s, 1, size(s)-1);


list[Modifier] modifiers(list[FieldModifier] fms)
  = [ m | modifier(m) <- fms ];
  
list[Qualifier] qualifiers(list[FieldModifier] fms)
  = [ q | qualifier(q) <- fms ];

list[ContentSpecifier] content(list[FieldModifier] fms)
  = [ c | content(c) <- fms ];

list[Expression] expressions(list[FieldModifier] fms)
  = [ *es | expressions(es) <- fms ];

 