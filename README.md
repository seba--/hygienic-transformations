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
metaprogramming language ([rascal-mpl.org](http://www.rascal-mpl.org)). API
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

1.  Install a fresh [Eclipse](http://www.eclipse.org), version Kepler.

2.  The unzipped artifact contains a full clone of the following repository:
    [https://github.com/tvdstorm/hygienic-transformations-ECOOP14](https://github.com/tvdstorm/hygienic-transformations-ECOOP14).

3.  From within the new Eclipse, go to Help > Install New Software...; click
    on Add... and then Local...; browse to the `update-site` directory of the
    cloned repository and press OK. After giving a name to the update site
    (doesn't matter what name), you'll be able to select Rascal for
    installation.  Finish the process by clicking on Yes when asked to restart
    Eclipse.

4.  In the restarted Eclipse, go to the File menu, and select Import..., then
    General > Existing Projects into Workspace, as root directory, select the
    `projects` directory in the cloned repo. Import all three projects there.
    You're now set up to explore the code, execute the tests and invoke
    `name-fix`.


## Project outline

The main code is stored in project Rascal-Hygiene. Below we summarize its
contents. For now you can ignore the projects `generated-derric` and
`generated-missgrant`; these are used for analyzing generated code in the case
studies.

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
* `src/org/derric_lang`: runtime classes needed for compiling the Derric
  language (copied), see [derric-lang.org](http://derric-lang.org)


## Name-fix: data structures and algorithm

The following files contain the data structures required by name-fix:

* `src/name/IDs.rsc`: Defines variable IDs as a list of source-code locations
  through string origins.
* `src/name/NameGraph.rsc`: Defines a name graph similar to the paper as a set
  of variable IDs (the nodes) and a mapping from node to node (the edges). We
  define a number of auxiliary functions for querying name graphs, such as
  `refOf : (ID, NameGraph) -> ID` or `nameAt : (ID, &T) -> str`. In the later,
  `&T` is a type variable `T` and `str` is Rascal's native string type.
* `src/name/Gensym.rsc`: Defines a gensym function `gensym : (str, set[str])
  -> str` that takes a base name and a set of used name and returns a fresh
  name not yet used. The fresh name has the form `base_n` where `n` is an
  integer.
* `src/name/figure/Figs.rsc`: Defines support for visualizing name graphs. If
  the call to `recordNameGraphFig` is included in the name-fix algorithm
  (remove the comments), Rascal will show the original name graph at the top
  and below the name-fixed name graph.

Name-fix itself is defined in the file `src/name/NameFix.rsc`. The code almost
literally corresponds to the code in the paper.

Finally, we provide a wrapper of name-fix to support name-fixing for
transformations that use lexical strings to represent the generated code. This
wrapper is defined in `src/name/NameFixString.rsc`. We use this wrapper to fix
names in generated string-based Java code.


## Running name-fix

To run Rascal code, start the Rascal console in Eclipse from the menu Rascal >
Start Console. The prompt `rascal>` indicates that the console has been
successfully launched.

The implementation of the name-fix algorithm resides in the file
`src/name/NameFix.rsc`. The signature of `nameFix` for tree-based program
transformations is as follows:

```
&T nameFix(type[&T <: node] astType, NameGraph Gs, &T t, NameGraph(&T) resolveT)
```

We explain how to call this function below. Note that the actual
implementation of the name-fix algorithm unfolds in an overloaded definition
of `nameFix` that has a sligtly more complicated singature, because it is
parametric over name-lookup and renaming functions. We use this to support
name-fix on strings, as implemented by the function `nameFixString` in
`src/name/NameFixString.rsc`.

Here is a complete example application of `nameFix`, that you can copy and
paste to the Rascal console to run. A longer walk through appears below.

    import lang::missgrant::base::AST;
    import lang::missgrant::base::Implode;
    import lang::missgrant::base::NameRel;
    import lang::simple::AST;
    import lang::simple::Compile;
    import lang::simple::NameRel;
    import name::NameGraph;
    import name::NameFix;

    Controller machine = load(|project://Rascal-Hygiene/input/door1.ctl|);
    NameGraph Gmachine = resolveNames(machine);
    Prog p = compile(machine);
    Prog pfixed = nameFix(#Prog, Gmachine, p, resolveNames);

This program first parses the statemachine from file `input/door1.ctl` into
the AST `machine`. Function `resolveNames` is overloaded and can be used for
state-machine ASTs of type `Controller` as well as for program ASTs of type
`Prog`. We first use `resolveNames` to compute the name graph of the state
machine. Then we compile the state machine to procedural code. Finally, we
call `nameFix`: The first argument is the type of the generated program, the
second argument is the name graph of the source program, the third argument is
the generated program, and the fourth argument is a function that computes the
name graph of target-language prorgrams.


### Detailed walk through

We now illustrate how to call `nameFix` through a running example of compiling
a state machine specification to a simple procedural program.  This should
suffice to demonstrate the general work flow.

1.  Start the Rascal console in Eclipse from the menu Rascal > Start Console
    after openning the project.  The prompt `rascal>` indicates that the
    console has been successfully launched.

2.  In the console, import all modules relevant to the syntax of the source
    and the target language.  These usually include the definitions of their
    concrete syntax, abstract syntax, parsers, pretty printers, name
    analyzers, compilers, etc.  In this example, we use state machines (SM) as
    source language and simple procedural programs (PROC) as target languages.
    These are the same languages used in Section 1 of the paper. We need the
    following modules:

        rascal> import lang::missgrant::base::AST;  // AST definition for SM
        rascal> import lang::missgrant::base::Implode;  // Parser for SM
        rascal> import lang::missgrant::base::NameRel;  // Name analysis for SM
    
        rascal> import lang::simple::AST;  // AST definition for PROC
        rascal> import lang::simple::Compile;  // Compiler from SM to PROC
        rascal> import lang::simple::Implode;  // Parser for PROC
        rascal> import lang::simple::NameRel;  // Name analysis for PROC
        rascal> import lang::simple::Pretty;  // Pretty printing for PROC
    

3. Folder `Rascal-Hygiene/input` contains example state machines. We can parse
   and load an existing state machine using function `load` (defined in
   `lang::missgrant::base::Implode`). For example, as the name suggests,
   compiling `missgrant-illcompiled.ctl` leads to inadvertent variable
   capture.

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
    
    You can see the variable capture in the duplicate declaration of variable
    `idle-dispatch`.

3.  Before we can call `nameFix` on the compiled program `p`, we need the name
    graph of the source program `m`.  It can be readily calculated:

    ```
    rascal> sNames = resolveNames(m);
    ```
    
    A name graph is a set of nodes and a mapping between nodes (the edges).
    You can click locations in the Rascal console to navigate to the
    referrenced source code and explore the name graph.

4.  As an optional step, we can calculate the name graph of the compiled
    program `p`, and then use the function `isCompiledHygienically` defined in
    `name::HygienicCorrectness` to verify that the compilation was indeed
    unhygienic.

    
        rascal> import name::HygienicCorrectness;
        rascal> tNames = resolveNames(p);
        rascal> isCompiledHygienically(sNames, tNames); // returns false
    

    Note that the name `resolveNames` is overloaded.

5.  Now we can call `nameFix` on `p`.

    ```
    rascal> import name::NameFix;
    rascal> p2 = nameFix(#Prog, sNames, p, resolveNames);
    ```

    Recall that the first argument to `nameFix` should be a reified type.  The
    operator `#` turns the type `Prog` of the target program to a value.
    Note again the overloaded `resolveNames` we pass to `nameFix` is the one
    for PROC.  Again, you can inspect the fixed program using the pretty
    printer:

    ```
    rascal> println(pretty(p2));
    ```

6.  At last, we can verify that `nameFix` indeed eliminates all captures and
    produces a program respecting the source-program bindings by calling
    `isCompiledHygienically` for the fixed program `p2`:

    ```
    rascal> isCompiledHygienically(sNames, resolveNames(p2));  // returns true
    ```

## Case studies

The above instructions show how to run name-fix interactively in the Rascal
console. Here we outline the main functions for each case study. We also have defined unit tests for each case study, which shows example usages of the involved functions. To run the unit tests defined in a module, import the module in the Rascal console and run

    rascal> :test 

The test definition in the module will be highlighted according to outcome of the test. To run all tests, execute `:test` after copy-pasting the following snippet into the console:

```
import name::tests::TestSubst;
import name::tests::TestInline;
import name::tests::TestLambLift;
import name::tests::TestStatemachineJava;
import name::tests::TestStatemachineSimple;
import name::tests::TestDerric;
import name::tests::TestNested;
```


### Substitution

Module `lang::simple::inline::Subst`.

Functions `subst` and `captureAvoidingSubst`, plus variants for the different syntactic forms of PROC.

Test module `name::tests::TestSubst`.

### Inlining

Module `lang::simple::inline::Inlining`.

Functions `inline`, `captureAvoidingInline`, and `captureAvoidingInline2`. Function `captureAvoidingInline2` implements capture-avoiding inlining via capture-avoiding substitution, whereas function `captureAvoidingInline` calls name-fix directly.

Test module `name::tests::TestInline`

### Lambda lifting

Module `lang::simple::locfun::Locfun`.

Function `liftLocfun`.

Test module `name::tests::TestLambLift`.


### State machines

Modules `lang::missgrant::base::AST`, `lang::missgrant::base::Implode`, `lang::missgrant::base::NameRel`.

Function `compile` in module `lang::missgrant::base::Compile` for compilation to Java and in module `lang::simple::Compile` for compilation to PROC.

Test modules `name::tests::TestStatemachineJava` and `name::tests::TestStatemachineSimple`.

##### Compilation to PROC

See the walk-through above.

##### Compilation to Java

Test module `name::tests::TestStatemachineJava`.

The previous case-study with state machines compiling the simple state machine language to a simple imperative language. In this case study, the state machines are compiled to Java using string templates. 

An example of a state machine that causes problems is `input/doors1-java-ill.ctl`. The reason is that it employs names that are also used by the compiler to implement the state machine. After importing the test module, invoke the function `compileIllCompiled1javaToDisk()` to inspect the incorrectly generated code. 

```
rascal> import name::tests::TestStatemachineJava;
rascal> compileIllCompiled1javaToDisk();
```

The output can be found in the `src` folder of the `generated-missgrant` project. As you will see, there are two compiler errors and various warnings. These are all caused by inadvertent name capture: in the run method, the synthesized names `current` and `token` capture the references to the constant declarations corresponding to the `current` and `token` states respectively. 

To see the repaired result, execute:
```
rascal> testIllCompiled1();
```

After running `name-fix` the synthesized names are renamed to `current_0` and `token_0` and name capture is avoided. 

### Derric

Test module `name::tests::TestDerric`.

Derric is domain-specific language (DSL) for describing (binary) file formats.  It is used to generate digital forensics analysis tools, such as file carvers. Example file format descriptions can be found in the folder `formats`. The format `minbad.derric` contains a description that, when run through the Derric compiler, produces valid Java code, but with the wrong semantics due to name capturing. 

After importing the test module `name::tests::TestDerric`, you can inspect the incorrect code in the package `org.derric_lang.validator.generated` in the `src` folder of the output project `generated-derric`. To see the result, execute:

```
rascal> import name::tests::TestDerric;
rascal> writeMinbadCompiled();
```

(Tip: press Ctrl-Shift-f or Cmd-Shift-f to format the code). As can be seen from the yellow marker, the private field `x` is never read. The reason can be found in method `S1`:

```
		long x;
		x = _input.unsigned().byteOrder(BIG_ENDIAN).readInteger(8);
		org.derric_lang.validator.ValueSet vs2 = new org.derric_lang.validator.ValueSet();
		vs2.addEquals(0);
		if (!vs2.equals(x))
			return noMatch();
``` 

The local variable `x` shadows the field. As a result the expression `vs2.equals(x)` uses the wrong `x`.

To see the fixed code run:

```
rascal> testMinBad();
```

The relevant code in the generated code now reads:

```
		long x_0;
		x_0 = _input.unsigned().byteOrder(BIG_ENDIAN).readInteger(8);
		org.derric_lang.validator.ValueSet vs2 = new org.derric_lang.validator.ValueSet();
		vs2.addEquals(0);
		if (!vs2.equals(x_0))
			return noMatch();
```

Note how the local `x` is renamed to `x_0`; as a result the `equals` expression now correctly uses the field `x`.




