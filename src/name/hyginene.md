# Question

What does **hygiene** mean in translation of a program in a source language to
a program in a target language?

# Intuition

The intuition for hygiene induces two conditions:

1. New names introduced into the target program by the translation should not
be accidentally captured by bindings introduced in the source program by the
user.

2. New bindings introduced into the target program by the translation should
not accidentally capture names introduced in the source prograam by the user.

The first can be guaranteed by generating fresh names that are distinct from
all names in the source program.  The second can be guaranteed by renaming
bound variables in the target program.

However, they are rather _static_ conditions.  The difficulty is to have the
translation always respect them, _especially_ when the user rename bound names
in the source program.  There are two ways to overcome the difficulty:

1. A new wave of translation is triggered (either automatically by the system
or manually by the user) whenever the user rename bound names in the source
program.

2. The translation is done once and for all; renewed bound names by the user
are somehow reflected in the target program.

Obviously, the second is preferred.  This suggest that we better have a
nameless representation respectively for both the source and the target
program, but with a record of user-given names in the source program.  This
way, renaming bound names in the target program is reduced to generating fresh
names.

# Approach

Traditional nameless representations using indices, global or local, seems to
fit only those _hierarchical_ binding constructs.  Whereas, using graphs to
record binding information seems more general.  With binding graphs comes a
new approach.  It involves the following steps:

1. Annotate the source program with binding information via a binding
analysis.  A graph is generated from these annotations, where every vertice of
the graph is a name, while every eadge directs from the binding site of a name
to a reference site of the name.

2. Translate the source abstract syntax tree.

3. Do a binding analysis on the target program, annotating it with binding
information.  Another binding graph is generated from these annotation.

4. Compare the two graphs.  If there exist two edges, one from the source
graph and one from the target graph, that are directed to the same vertice but
from two different vertices, then some binding in the target program captures
a name in the source program.  The source vertice of the edge from the target
graph is the bound name introduced by the translation that causes the capture.
To avoid the capture in the target graph, first restore all edges signifying
capture to their counterparts in the source graph, and then rename all
vertices of the remaining edges fromt he culprit vertice.

The benefits of this approach are:

1. It separates renaming from translation.

2. It poses no restriction on translation.

# Puzzles

1. What if the source language and the target language are the same?  Or the
source language is the target language plus extensions?  Or the source
language and the meta-language are the same?

2. How to formalize the specification so as to guarantee alpha-equivalence
over translation?

