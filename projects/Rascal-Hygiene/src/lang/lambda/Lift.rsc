module lang::lambda::Lift

extend lang::lambda::Syntax;
extend lang::lambda::Names;

start syntax Prog = prog: (Def ";")* defs Exp main;
syntax Def = def: Id name "=" Exp body;

data Prog = prog(list[Def] defs, Exp main);
data Def = def(str name, Exp body);

str printProg(prog(defs, main)) = ("" | it + s | s <- ["<name> = <print(body)>;\n" | def(name, body) <- defs]) + "<print(main)>\n";
Prog parseProg(str src){
  file = |project://Rascal-Hygiene/output/| + "stdin<nextParse>.lambda";
  nextParse = nextParse + 1;
  writeFile(file, src);
  return implode(#Prog, parse(#start[Prog], src, file));
}

NameGraph resolveProg(Prog p) = resolve(p);
NameGraph resolve(prog(defs, main)) {
  topscope = (name:getID(name) | def(name,_) <- defs);
  <V,E> = <topscope<1>,()>;
  for (def(n,e) <- defs) {
    <Vd,Ed> = resolve(e, topscope);
    <V,E> = <V + Vd, E + Ed>;
  }
  <Vmain,Emain> = resolve(main, topscope);
  return <V + Vmain, E + Emain>;
}

Prog liftLambdas(Exp e) = liftLambdas(prog([], e), resolve(e));
Prog liftLambdas(prog(defs, main), G) {
  lifted = [];
  newvarCount = 0;
  str newvar() {
    v = "f<newvarCount>";
    newvarCount += 1;
    return v;
  }
  
  liftedMain = visit(main) {
    case f:lambda(_,_): {
      free = freevars(f, G);
      name = newvar();
      res = var(name);
      for (v <- free) {
        f = lambda(v, f);
        res = app(res, var(v));
      }
      lifted += def(name, f);
      insert res;
    }
  };
  
  return prog(defs + lifted, liftedMain);
}

