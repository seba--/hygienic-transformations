# Capture-avoiding and Hygienic Program Transformations

By Sebastian Erdweg, Tijs van der Storm, and Yi Dai.

Accompanying our article to appear at ECOOP'14.

## Summary

Program transformations in terms of abstract syntax trees compromise
referential integrity by introducing variable capture. Variable capture occurs
when in the generated program a variable declaration accidentally shadows the
intended target of a variable reference. Existing transformation systems
either do not guarantee the avoidance of variable capture or impair the
implementation of transformations.

We present an algorithm called name-fix that automatically eliminates variable
capture from a generated program by systematically renaming variables.
name-fix is guided by a graph representation of the binding structure of a
program, and requires name-resolution algorithms for the source language and
the target language of a transformation. name-fix is generic and works for
arbitrary transformations in any transformation system that supports origin
tracking for names. We verify the correctness of name-fix and identify an
interesting class of transformations for which name-fix provides hygiene. We
demonstrate the applicability of name-fix for implementing capture-avoiding
substitution, inlining, lambda lifting, and compilers for two domain-specific
languages.


## Rascal

The name-fix algorithm and our case studies are implemented in the Rascal
metaprogramming language ([rascal-mpl.org](http://rascal-mpl.org)). API
documentation for standard Rascal functions and types is available online
([http://tutor.rascal-mpl.org/Rascal/Rascal.html](http://tutor.rascal-mpl.org/Rascal/Rascal.html)).
In this project, we particularly use Rascal's support for [syntax definitions
and
parsing](http://tutor.rascal-mpl.org/Rascal/Rascal.html#/Rascal/Declarations/SyntaxDefinition/SyntaxDefinition.html).
Program transformations and the name-fix algorithm itself are standard Rascal
[functions](http://tutor.rascal-mpl.org/Rascal/Rascal.html#/Rascal/Concepts/Functions/Functions.html).

We also use Rascal's support for unit testing and quickchecking. In
particular, Rascal interprets function that use the modifier `test` as test
specification. If the function takes arguments, Rascal will randomly generate
arguments for testing (similar to quickcheck). All loaded unit tests can be
run by typing `:test` into the Rascal console (see below for detailed
instructions).


## Installation instructions

- Install a fresh [Eclipse](http://www.eclipse.org), version Kepler.

- Clone the following repository: [https://github.com/tvdstorm/hygienic-transformations-ECOOP14]

- From within the new Eclipse, go to Help->Install new software; click
  on Add.. and select local. Browse to the `update-site` directory of
  the cloned repository and press Ok. After giving a name to the
  update site (doesn't matter what name), you'll be able to select
  Rascal for installation. Finish the process by clicking yes when
  asked to restart Eclipse.

- In the restarted Eclipse, go to the File menu, and select
  Import... As root directory, select the `projects` directory in the
  cloned repo. Import all three projects there. You're now set up to
  explore the code, execute the tests and invoke `name-fix`.


## Project outline

The main code is stored in project Rascal-Hygiene. Below we summarize
its contents. For now you can ignore the projects `generated-derric` and `generated-missgrant`; these are used analyzing generated code in the case studies. 

The two most important folders are `src/name` and
`src/name/tests`. The former contains the implementation of name-fix
and the latter contains unit tests for all case studies.

These are the most important folders in the repository:

* `input`: Example state machines
* `output`: Generated state machines
* `format`: Example format descriptors for the Derric case study
* `src`: Source code of name-fix and case studies
* `src/name`: Implementation of name-fix and required data structures
* `src/name/tests`: Unit tests for all case studies.
* `src/lang/simple`: Implementation of the simple procedural language
* `src/lang/java:`: Name analysis for Java using Eclipse JDT
* `src/lang/missgrant:`: Implementation of the state-machine language
* `src/lang/derric`: Implementation of the Derric language (copied),
  see [derric-lang.org](http://derric-lang.org)
* `src/org/derric_lang`: runtime classes needed for compiling the Derric language
  (copied), see [derric-lang.org](http://derric-lang.org)


## Name-fix: data structures and algorithm

The following files contain the data structures required by name-fix:

* `src/name/IDs.rsc`: Defines variable IDs as a list of source-code
  locations through string origins.
* `src/name/NameGraph.rsc`: Defines a name graph similar to the paper
  as a set of variable IDs (the nodes) and a mapping from node to node (the
  edges). We define a number of auxiliary functions for querying name graphs,
  such as `refOf : (ID, NameGraph) -> ID` or `nameAt : (ID, &T) -> str`. In
  the later, `&T` is a type variable `T` and `str` is Rascal's native string
  type.
* `src/name/Gensym.rsc`: Defines a gensym function `gensym :
  (str, set[str]) -> str` that takes a base name and a set of used name and
  returns a fresh name not yet used. The fresh name has the form `base_n`
  where `n` is an integer.
* `src/name/figure/Figs.rsc`: Defines support for visualizing name
  graphs. If the call to `recordNameGraphFig` is included in the name-fix
  algorithm (remove the comments), Rascal will show the original name graph at
  the top and below the name-fixed name graph.

Name-fix itself is defined in the file `src/name/NameFix.rsc`. The code
almost literally corresponds to the code in the paper.

Finally, we provide a wrapper of name-fix to support name-fixing for
transformations that use lexical strings to represent the generated code. This
wrapper is defined in `src/name/NameFixString.rsc`. We use this wrapper
to fix names in generated string-based Java code.


## Running name-fix

The implementation of the name-fix algorithm resides in the file
`src/name/NameFix.rsc`.  To run name-fix boils down to call the Rascal
function `nameFix` in this file.  The interface for `nameFix` is

```
&T nameFix(type[&T <: node] astType, NameGraph Gs, &T t, NameGraph(&T) resolveT)
```

A call to `nameFix` on tree-based program representation should conform to
this interface.  Note that `nameFix` has a second sligtly more complicated
interface.  It is for string-based program representation, but not meant to be
called directly.  Instead, the wrapper `nameFixString` provided in
`src/name/NameFixString.rsc` should be preferred.  Since
`nameFixString` mirrors this simpler interface of `nameFix` shown above, the
instructions on calling `nameFix` given below also apply when calling
`nameFixString`.

First, note that `nameFix` is parametric over the target program
representation, as indicated by the type variable `&T`.

Second, `nameFix` expects four arguments.  The second is supposed to be the
name graph (to be bound to the parameter `Gs`) of the source program, the
third the target program (to be bound to `t`), and the fourth a name analyzer
(as a function, to be bound to `resolveT`) for the target language.  The first
argument is supposed to be a subtype of Rascal's built-in tree type `node`, as
constrained by `&T <: node`.  More precisely, it should be a _reifed_ type, as
required by `type[ ]`.  This declaration together with a device to reify a
type (see below) is needed to turn a type to a value so as to be passed as
argument, because normal types are not treaded as values in Rascal.
Essentially this supports more finely-distinguished return types.

We now illustrate how to call `nameFix` through a running example of compiling
a state machine specification to a simple procedural program.  This should
suffice to demonstrate the general work flow.

1.  Start the Rascal console in Eclipse from the menu Rascal > Start Console
    after openning the project.  The prompt `rascal>` indicates that the
    console has been successfully launched.

2.  In the console, import all modules relevant to the syntax of the source
    and the target language.  These usually include the definitions of their
    concrete syntax, abstract syntax, parsers, pretty printers, name
    analyzers, compilers, etc.  For our particular example, call the source
    language SMSL (State Machine Specification Language) and the target
    language SPL (Simple Procedural Language), we need run the following
    import statements (for brevity, all the replies are ommited):

    ```
    rascal> import lang::missgrant::base::AST;  // AST definition for SMSL
    rascal> import lang::missgrant::base::Implode;  // Rascal tree to SMSL AST
    rascal> import lang::missgrant::base::NameRel;  // Name analysis for SMSL

    rascal> import lang::simple::AST;  // AST definition for SPL
    rascal> import lang::simple::Compile;  // Compiler from SMSL to SPL
    rascal> import lang::simple::Implode;  // Rascal tree to SPL AST
    rascal> import lang::simple::NameRel;  // Name analysis for SPL
    rascal> import lang::simple::Pretty;  // Pretty printing for SPL
    ```

    With all these modules imported.  We can try to `load` (a function defined
    in `lang::missgrant::base::Implode`) a sample state-machine specification
    identified by the URI `|project://Rascal-Hygiene/input/missgrant-illcompiled.ctl|`. As the name suggests, compiling this program leads to inadvertent name capture.

    ```
    rascal> m = load(|project://Rascal-Hygiene/input/missgrant-illcompiled.ctl|);
    ```

    We can go ahead compiling the loaded program:

    ```
    rascal> Prog p = compile(m);
    ```
    
    To inspect the textual representation of `p`, call the pretty printer:
    
    ```
    rascal> println(pretty(p));
    ```

3.  Before we can call `nameFix` on the compiled program `p`, we need the name
    graph of the source program `m`.  It can be readily calculated:

    ```
    rascal> sNames = resolveNames(m);
    ```

4.  As an optional step, we can calculate the name graph of the compiled
    program `p`, and then use the function `isCompiledHygienically` defined in
    `name::HygienicCorrectness` (of course before using it we need first
    import this module) to check whether the compilation is hygienic so as to
    decide whether we need call `nameFix`:

    ```
    rascal> tNames = resolveNames(p);
    rascal> import name::HygienicCorrectness;
    rascal> isCompiledHygienically(sNames, tNames);
    ```

    Note that the name `resolveNames` is overloaded.  For the sample SMSL
    program we chose, as the result of `isCompiledHygienically` suggests, compiling this sampe state machin is not hygienic. 

5.  Now we can call `nameFix` on `p`.

    ```
    rascal> import name::NameFix;
    rascal> p2 = nameFix(#Prog, sNames, p, resolveNames);
    ```

    Recall that the first argument to `nameFix` should be a reified type.  The
    operator `#` turns the type `Prog` of the target program to a value.
    Again note the overloaded `resolveNames` is the one for SPL. Again, you can inspect the fixed program using the pretty printer:
    
    ```
    rascal> println(pretty(p2));
    ```

6.  At last, we can verify that `nameFix` indeed eliminates all captures and
    produces a program respecting hygiene by calling `isCompiledHygienically`
    again but this time on the name graph of the source program and that of
    the new program `p2`:

    ```
    rascal> isCompiledHygienically(sNames, resolveNames(p2));
    ```

## Case studies

The above instructions show how to run name-fix interactively in the Rascal
console.  Alternatively, we may want to save an interactive session into a
Rascal module for regression test.  We do this for all our case studies.
These test modules reside in the directory `src/name/tests`:

- `src/name/tests/Test.rsc`: tests for some state machines coming
  together with the state-machine language implementation
- `src/name/tests/TestDerric.rsc`: tests for the Derric language
- `src/name/tests/TestDoor.rsc`: tests for the door state machine
- `src/name/tests/TestJava.rsc`: tests for the Java language
- `src/name/tests/TestNested.rsc`: tests for the simple procedural
  language
- `src/name/tests/TestString.rsc`: tests for _WHAT_?

To run these tests, simply import them in the Rascal console.  For example,

```
rascal> import name::tests::Test;
```

will import all the definitions in the file `src/name/tests/Test.rsc`.
A _nullary_ function definition in the module usually wraps a test.  Calling
such a function runs the test.  For example, in `Test.rsc`, the nullary
function `nameFix1` wraps the interactive test we have seen above.  Executing
`nameFix1();` in the Rascal console reruns the test.  Tests for all our case
studies can be run like this.

