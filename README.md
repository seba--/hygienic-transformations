# Capture-avoiding and Hygienic Program Transformations

By Sebastian Erdweg, Tijs van der Storm, and Yi Dai.

Accompanying our article to appear at ECOOP'14.

## Summary

Program transformations in terms of abstract syntax trees compromise referential integrity by introducing variable capture. Variable capture occurs when in the generated program a variable declaration accidentally shadows the intended target of a variable reference. Existing transformation systems either do not guarantee the avoidance of variable capture or impair the implementation of transformations.

We present an algorithm called name-fix that automatically eliminates variable capture from a generated program by systematically renaming variables. name-fix is guided by a graph representation of the binding structure of a program, and requires name-resolution algorithms for the source language and the target language of a transformation. name-fix is generic and works for arbitrary transformations in any transformation system that supports origin tracking for names. We verify the correctness of name-fix and identify an interesting class of transformations for which name-fix provides hygiene. We demonstrate the applicability of name-fix for implementing capture-avoiding substitution, inlining, lambda lifting, and compilers for two domain-specific languages.


## Rascal

The name-fix algorithm and our case studies are implemented in the Rascal metaprogramming language [(rascal-mpl.org)](http://rascal-mpl.org). API documentation for standard Rascal functions and types is available online [(http://tutor.rascal-mpl.org/Rascal/Rascal.html)](http://tutor.rascal-mpl.org/Rascal/Rascal.html). In this project, we particularly use Rascal's support for [syntax definitions and parsing](http://tutor.rascal-mpl.org/Rascal/Rascal.html#/Rascal/Declarations/SyntaxDefinition/SyntaxDefinition.html). Program transformations and the name-fix algorithm itself are standard Rascal [functions](http://tutor.rascal-mpl.org/Rascal/Rascal.html#/Rascal/Concepts/Functions/Functions.html).

We also use Rascal's support for unit testing and quickchecking. In particular, Rascal interprets function that use the modifier `test` as test specification. If the function takes arguments, Rascal will randomly generate arguments for testing (similar to quickcheck). All loaded unit tests can be run by typing `:test` into the Rascal console (see below for detailed instructions).


## Repository outline

The two most important folders are `rascal/src/name` and `rascal/src/name/test`. The former contains the implementation of name-fix and the latter contains unit tests for all case studies.

These are the most important packages in the repository:

* `rascal`: Rascal Eclipse project with the name-fix algorithm and all case studies
* `rascal/input`: Example state machines
* `rascal/output`: Generated state machines
* `rascal/format`: Example format descriptors for the Derric case study
* `rascal/src`: Source code of name-fix and case studies
* `rascal/src/name`: Implementation of name-fix and required data structures
* `rascal/src/name/tests`: Unit tests for all case studies.
* `rascal/src/lang/simple`: Implementation of the simple procedural language
* `rascal/src/lang/java:`: Name analysis for Java using Eclipse JDT
* `rascal/src/lang/missgrant:`: Implementation of the state-machine language
* `rascal/src/lang/derric`: Implementation of the Derric language (copied), see [derric-lang.org](http://derric-lang.org)
* `rascal/src/org/derric_lang`: Implementation of the Derric language (copied), see [derric-lang.org](http://derric-lang.org)


## Name-fix: data structures and algorithm

The following files contain the data structures required by name-fix:

* `rascal/src/name/IDs.rsc`: Defines variable IDs as a list of source-code locations through string origins.
* `rascal/src/name/NameGraph.rsc`: Defines a name graph similar to the paper as a set of variable IDs (the nodes) and a mapping from node to node (the edges). We define a number of auxiliary functions for querying name graphs, such as `refOf : (ID, NameGraph) -> ID` or `nameAt : (ID, &T) -> str`. In the later, `&T` is a type variable `T` and `str` is Rascal's native string type.
* `rascal/src/name/Gensym.rsc`: Defines a gensym function `gensym : (str,set[str]) -> str` that takes a base name and a set of used name and returns a fresh name not yet used. The fresh name has the form `base_n` where `n` is an integer.
* `rascal/src/name/figure/Figs.rsc`: Defines support for visualizing name graphs. If the call to `recordNameGraphFig` is included in the name-fix algorithm (remove the comments), Rascal will show the original name graph at the top and below the name-fixed name graph.

Name-fix itself is defined in file `rascal/src/name/NameFix.rsc`. The code almost literally corresponds to the code in the paper.

Finally, we provide a wrapper of name-fix to support name-fixing for transformations that use lexical strings to represent the generated code. This wrapper is defined in `rascal/src/name/NameFixString.rsc`. We use this wrapper to fix names in generated string-based Java code.


## Running name-fix




## Case studies












