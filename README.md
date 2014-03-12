# Capture-avoiding and Hygienic Program Transformations

By Sebastian Erdweg, Tijs van der Storm, and Yi Dai.

Accompanying our article to appear at ECOOP'14.

## Summary

Program transformations in terms of abstract syntax trees compromise referential integrity by introducing variable capture. Variable capture occurs when in the generated program a variable declaration accidentally shadows the intended target of a variable reference. Existing transformation systems either do not guarantee the avoidance of variable capture or impair the implementation of transformations.

We present an algorithm called name-fix that automatically eliminates variable capture from a generated program by systematically renaming variables. name-fix is guided by a graph representation of the binding structure of a program, and requires name-resolution algorithms for the source language and the target language of a transformation. name-fix is generic and works for arbitrary transformations in any transformation system that supports origin tracking for names. We verify the correctness of name-fix and identify an interesting class of transformations for which name-fix provides hygiene. We demonstrate the applicability of name-fix for implementing capture-avoiding substitution, inlining, lambda lifting, and compilers for two domain-specific languages.


## Rascal

The name-fix algorithm and our case studies are implemented in the Rascal metaprogramming language [(rascal-mpl.org)](http://rascal-mpl.org). API documentation for standard Rascal functions and types is available online [(http://tutor.rascal-mpl.org/Rascal/Rascal.html)](http://tutor.rascal-mpl.org/Rascal/Rascal.html). In this project, we particularly use Rascal's support for [syntax definitions and parsing](http://tutor.rascal-mpl.org/Rascal/Rascal.html#/Rascal/Declarations/SyntaxDefinition/SyntaxDefinition.html). Program transformations and the name-fix algorithm itself are standard Rascal [functions](http://tutor.rascal-mpl.org/Rascal/Rascal.html#/Rascal/Concepts/Functions/Functions.html).


## Repository outline

The two most important folders are `rascal/src/name` and `rascal/src/name/test`. The former contains the implementation of name-fix and the latter contains unit tests for all case studies.

* `rascal`: Rascal Eclipse project with the name-fix algorithm and all case studies
* `rascal/input`: Example state machines
* `rascal/output`: Generated state machines
* `rascal/format`: Example format descriptors for the Derric case study
* `rascal/src`: Source code of name-fix and case studies
* `rascal/src/name`: Implementation of name-fix and required data structures
* `rascal/src/name/test`: Unit tests for all case studies.
* `rascal/src/lang/simple`: Implementation of the simple procedural language
* `rascal/src/lang/java:`: Name analysis for Java using Eclipse JDT
* `rascal/src/lang/missgrant:`: Implementation of the state-machine language
* `rascal/src/lang/derric`: Implementation of the Derric language (copied), see [derric-lang.org](http://derric-lang.org)
* `rascal/src/org/derric_lang`: Implementation of the Derric language (copied), see [derric-lang.org](http://derric-lang.org)

