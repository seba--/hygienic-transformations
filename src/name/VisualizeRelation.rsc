module name::VisualizeRelation

import name::Relation;
import vis::Figure;
import vis::KeySym;
import vis::Render;
import util::Editors;


Figure toFigure(NameRel names) {
  return graph(
     [box(text("<n>: <x.begin.line>, <x.begin.column>"), left(), top(), 
          id("<x.offset>"), 
          
          onMouseDown(bool (int btn, map[KeyModifier,bool] m) {
             edit(x);
          }))
          
           | <n, x> <- names<0,1> + names<0,2> ],
          
     [edge("<u.offset>", "<d.offset>", triangle(10,fillColor("black"))) | <n, u, d> <- names ], 
     hint("layered"), width(900), height(1000), top(), gap(70));
}

void renderNames(NameRel names) = 
  render(toFigure(names));
