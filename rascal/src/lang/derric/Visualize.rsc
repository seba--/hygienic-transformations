module lang::derric::Visualize

import vis::Figure;

import lang::derric::FileFormat;
import lang::derric::GenerateDerric;


// extension
data Symbol = empty();


public Figure formatToFigure(FileFormat format) {  
  Figure term2figure(Term t) = vcat([text(t.name, fontBold(true)), *[ text(writeField(f)) | f <- t.fields]]);
  
  nfa = regexp2nfa(seq(format.sequence));
  structs = ( t.name: t | /Term t <- format.terms );
  nodes = [ellipse(text("<q>"), id("<q>"),size(20)) | q <- nfa.trans<0> + nfa.trans<2> ];
  nodes += [ ellipse(id("<q1>-<q2>"),size(1)) | <q1, "", q2> <- nfa.trans ];
  used = { n | /term(n) <- format.sequence };
  nodes += [ box(term2figure(t), id(t.name)) | /Term t <- format.terms, t.name in used ];
  
  edges = [ edge("<q1>", "<n>", toArrow(triangle(10))) | <q1, n, _> <- nfa.trans, n != "" ]
        + [ edge("<n>", "<q2>", toArrow(triangle(10))) | <_, n, q2> <- nfa.trans, n != "" ];
  edges += [ edge("<q1>", "<q1>-<q2>") | <q1, "", q2> <- nfa.trans ]
        + [ edge("<q1>-<q2>", "<q2>") | <q1, "", q2> <- nfa.trans ];
  return graph(nodes, edges, hint("layered"), gap(40), size(1024, 768));
}


alias State = int;
alias Label = str;

alias Trans[&State] = rel[&State from, Label label, &State to];

data FSM = fsm(State init, set[State] finals, Trans[State] trans);

private str epsilon = "";

public FSM regexp2nfa(Symbol re) {
  //dynamic state = 0;

  int STATE = 0;
  int newState() {
    STATE += 1;
    return STATE;
  }


  FSM union(FSM fsm1, FSM fsm2) {
    q0 = newState();
    qf = newState();
    return fsm(q0, 
               {qf},
               fsm1.trans + fsm2.trans +
                  {<q0, epsilon, fsm1.init>, 
                   <q0, epsilon, fsm2.init>}
                   
                  + { <q, epsilon, qf> | q <- fsm1.finals + fsm2.finals }
                   );
      
  }
  
  FSM union2(set[FSM] fsms) {
    q0 = newState();
    qf = newState();
    
    return fsm(q0, {qf},
       ( {} | it + f.trans | f <- fsms )
       + { <q0, epsilon, f.init> | f <- fsms }
       + { <q, epsilon, qf> | f <- fsms, q <- f.finals });
  }

  FSM concat(FSM fsm1, FSM fsm2) =
    fsm(fsm1.init, fsm2.finals, fsm1.trans + fsm2.trans + 
            {<qf, epsilon, fsm2.init> | qf <- fsm1.finals });  

  FSM opt(FSM fsm1) {
    q0 = newState();
    qf = newState();
    return fsm(q0, {qf}, fsm1.trans + {<q0, epsilon, qf>, <q0, epsilon, fsm1.init>}
      + { <q, epsilon, qf> | q <- fsm1.finals });
  }

  FSM kleene(FSM x) {
    q0 = newState();
    qf = newState();
    return fsm(q0, {qf}, x.trans + {<q0, epsilon, x.init>} +
       { <q, epsilon, qf> | q <- x.finals} 
       + { <qf, epsilon, q0> });  
  }

  FSM atomic(Label e) {
    s = newState();
    f = newState();
    return fsm(s, {f}, {<s, e, f>});
  }

  FSM sym2nfa(Symbol sym) {
      switch (sym) {
      case term(str e) :
        return atomic(e);
      
      case seq([sym1]):
        return sym2nfa(sym1); 
      case seq([sym1, sym2, *syms]): 
        return concat(sym2nfa(sym1), sym2nfa(seq([sym2, *syms])));
      
      case optional(sym1):
        return opt(sym2nfa(sym1));
      
      case not(anyOf(syms)): // special case
        return sym2nfa(anyOf({ t | /t:term(_) <- re, t notin syms}));
      
      case anyOf({sym1}):
        return sym2nfa(sym1);
      case anyOf({sym1, sym2, *syms}):
        return union2( { sym2nfa(s) | s <- {sym1, sym2, *syms} });
      
      case iter(sym1): 
        return kleene(sym2nfa(sym1));
         
      //case choice(RegExp r1, RegExp r2):
      //  return unionFSM(sym2nfa(r1), sym2nfa(r2));
      //case empty(): 
      //  return atomic("empty");
      default:
        throw "Unsupported symbol: <sym>";
      }
   }
   
   return sym2nfa(re);
}








