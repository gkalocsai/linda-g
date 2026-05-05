# Specification — Position-based Textual DAG Description Language

Summary: A linear text sequence describes a directed acyclic graph (DAG). Each token is a node: a string literal, a primitive/function call, or a donor/acceptor placeholder. Primitive calls use positional, relative references to earlier nodes: 1 = the immediately preceding node, 2 = the one before that, etc. Numbers listed left-to-right map to primitive parameter order. Every node exposes two outputs: a boolean (.bool) and a string (.str). String literals are quoted and always have .bool = true.

1) Concepts and high-level rules
- The program is a sequence of nodes processed left-to-right. Node indices increase with position.
- All relative references point backward only; this guarantees acyclicity.
- A primitive call is written in parentheses; inside are integer arguments (relative references) followed by the primitive name. Example patterns:
  - `"2" (1 1 SAME)`
  - `"Sarah" "John" (1 2 LOVES)` → John loves Sarah
- If a relation is mutual, represent it with separate nodes (no cycles).

2) Syntax (informal / EBNF-like)
- program ::= node*
- node ::= string_literal | donor | acceptor | primitive_call
- string_literal ::= '"' <characters> '"'
- donor ::= '<' TYPE_ID '>'
- acceptor ::= '<<' TYPE_ID '>>'
- primitive_call ::= '(' arg* PRIMITIVE_NAME ')'
- arg ::= INTEGER ; positive (1,2,3,...)
- PRIMITIVE_NAME ::= e.g. SAME, CONCAT, PLUS, MINUS, RNDINT, ...
- TYPE_ID ::= non-empty string (implementation may allow quoted ids)
- Tokens must be separated by whitespace/newline. String literal content is opaque.

3) Node outputs and types
- Every node provides:
  - .bool — boolean output (always present)
  - .str  — string value output (always present)
- String literal:
  - .str = literal contents
  - .bool = true
- Primitive nodes compute .str and .bool per their own semantics.
- Donor/acceptor are placeholders (see section 6).

4) Reference resolution
- When evaluating a primitive_call at position p, each numeric argument n refers to the node at position (p - n).
- Argument order: the numeric args, read left-to-right, correspond to primitive parameters 1..k.
- Error if any referenced node position < 1.
- Arity must match the primitive's required number of args.

5) Base primitives
General rule: primitives that transform data typically read their inputs from argX.str. Logical primitives usually set .bool based on .str equality/tests. On parse/compute failures, .bool may be set false and .str to an empty or error sentinel.

- RNDINT
  - Syntax: `( RNDINT )`
  - Arity: 0
  - Behavior: generates a random integer (implementation-specified range).
  - Output: .str = decimal string of the integer; .bool = true

- CONCAT
  - Syntax: `( arg1 arg2 CONCAT )`
  - Arity: 2
  - Inputs: arg1.str, arg2.str
  - Output: .str = arg1.str + arg2.str ; .bool = true

- SAME
  - Syntax: `( arg1 arg2 SAME )`
  - Arity: 2
  - Inputs: arg1.str, arg2.str
  - Output: .bool = (arg1.str == arg2.str) ; .str conventionally returns arg1.str (or implementation may choose empty)

- PLUS
  - Syntax: `( arg1 arg2 PLUS )`
  - Arity: 2
  - Inputs: argN.str parsed as numbers (integer or floating; implementation choice)
  - Output: If parse succeeds: .str = string(formal sum); .bool = true. If parse fails: .bool = false; .str = "" (or error marker)

- MINUS
  - Syntax and behavior analogous to PLUS; result = arg1 - arg2.

6) Donor and Acceptor (merge placeholders)
- Represented in source:
  - Acceptor: `<<TYPE_ID>>`
  - Donor: `<TYPE_ID>`
- Semantics: placeholders intended for merging subgraphs. They are no-op nodes at runtime: if they have an input, they copy their input node's outputs; if not, they produce empty/false outputs per implementation convention.
- TYPE_ID is a string that must match between donor and acceptor for compatibility.
- Merge (high-level algorithm):
  1. Identify the donor node D in donor graph G_don and the acceptor node A in acceptor graph G_acc. Both must share TYPE_ID.
  2. Define the donor prefix to import as all nodes in G_don from the start up to and including D (i.e., the donor is the last node in the donor prefix).
  3. Insert the donor prefix nodes into G_acc immediately before A (so inserted nodes precede A).
  4. Replace A by the inserted donor-prefix's last node (the donor node instance).
  5. After insertion, relative references internal to the inserted prefix remain valid because their relative ordering is preserved; external references in G_acc that previously pointed to A now point to the inserted donor-last-node.
- Requirement: Donor.TYPE_ID must equal Acceptor.TYPE_ID; otherwise merge is rejected.
- Merge must not create cycles if donor prefix only references nodes within itself and nodes that will appear before the insertion point.

7) Evaluation model
- Single left-to-right pass (parse/resolve/evaluate).
- When encountering a primitive, resolve each numeric arg to the corresponding previous node and use the required outputs (.str or .bool) per primitive semantics.
- The final program result is the last node's outputs (.str and .bool) unless the embedding system specifies otherwise.
- Random primitives (RNDINT) are implementation-dependent regarding determinism (seedable or not).

8) Errors and validation
- Syntax errors: malformed tokens, unclosed quotes, missing parentheses.
- Reference errors: numeric arg points before the start (out-of-range).
- Arity errors: wrong number of args for a primitive.
- Merge errors: donor/acceptor TYPE_ID mismatch, invalid donor prefix, or attempted merge that would create cycles.
- Runtime/errors inside primitives: numeric parse failure for PLUS/MINUS (set .bool=false), other primitive-specific errors.
- Implementations should emit clear diagnostics (position, expected arity, referenced index).

9) Examples
- Mutual relations:
  - `"Sarah" "John" (1 2 LOVES)`
    - Node1: "Sarah" (.str="Sarah", .bool=true)
    - Node2: "John" (.str="John", .bool=true)
    - Node3: (1 2 LOVES) → args: 1→Node2, 2→Node1 → LOVES(John, Sarah)
- Reciprocal:
  - `"Sarah" "John" (1 2 LOVES) (3 2 LOVES)`
    - Two separate LOVES nodes model mutual liking without graph cycles.
- Numeric/random example (as given in source):
  - `(RNDINT) (RNDINT) (1 2 PLUS) (1 3 PLUS) (1 2 MINUS) (5 1 SAME)`
    - Interpreting relative indices strictly at each primitive position; final SAME compares values according to their resolved positions.
- String building:
  - `"tehén" "fej" (2 1 CONCAT) "en" (2 1 CONCAT)`
    - Node1: "tehén"
    - Node2: "fej"
    - Node3: (2 1 CONCAT) → CONCAT(Node1, Node2) = "tehénfej"
    - Node4: "en"
    - Node5: (2 1 CONCAT) → CONCAT(Node3, Node4) = "tehénfejen"
    - Node5: .str = "tehénfejen", .bool = true