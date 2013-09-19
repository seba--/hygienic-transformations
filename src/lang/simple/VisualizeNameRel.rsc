module lang::simple::VisualizeNameRel

import lang::missgrant::base::NameRel;
import vis::Figure;
import vis::Render;


Figure toFigure(rel[str name, loc use, loc def] names) {
  return graph(
     [box(text("<n>: <x.begin.line>, <x.begin.column>"), left(), top(), 
          id("<x.offset>")) | <n, x> <- names<0,1> + names<0,2> ],
          
     [edge("<u.offset>", "<d.offset>", triangle(10,fillColor("black"))) | <n, u, d> <- names ], 
     hint("layered"), width(900), height(1000), top(), gap(70));
}

void renderNames(rel[str name, loc use, loc def] names) = 
  render(toFigure(names));
