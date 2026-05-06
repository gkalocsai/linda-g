# Specification — Position-based Textual DAG Description Language

**Summary:** A linear text sequence describes a directed acyclic graph (DAG). Each token is a node: a string literal, a primitive/function call, or a donor/acceptor placeholder. Primitive calls use positional, relative references to earlier nodes: 1 = the immediately preceding node, 2 = the one before that, etc. Numbers listed left-to-right map to primitive parameter order. Every node exposes one output: a string (`.str`). String literals are quoted.

### 1) Concepts and High-Level Rules
- The program is a sequence of nodes processed left-to-right. Node indices increase with position.
- All relative references point backward only; this guarantees acyclicity.
- A primitive call is written in parentheses; inside are integer arguments (relative references) followed by the primitive name. 
  - Example patterns: 
    - `"2" (1 1 SAME)`
    - `"Sarah" "John" (1 2 LOVES)` $\rightarrow$ John loves Sarah
- If a relation is mutual, represent it with separate nodes (no cycles).
- **The Global Connection Registry:** A persistent store of directed facts. Unlike Computational primitives which transform data, Connection and Query primitives interact with this registry using **Value-based Identity**.
The registry persist across multiple different programs run in the same session.
- **Value-based Identity:** A node's identity is its `.str` value. If two nodes have the same `.str` value, they are treated as the same entity in the Registry.
- **Primitive Classification:**
  - **Computation (`!NAME`):** Reads `.str` values $\rightarrow$ Produces a new `.str` value.
  - **Connection (`NAME`):** Reads `.str` values $\rightarrow$ Records a directed fact `(Source, Target, Relation)` in the Registry $\rightarrow$ Produces `"T"`.
  - **Query (`?NAME`):** Reads `.str` values $\rightarrow$ Checks Registry for the directed fact $\rightarrow$ Produces `"T"` or `"F"`.

### 2) Syntax (Informal / EBNF-like)
- `program ::= node*`
- `node ::= string_literal | donor | acceptor | primitive_call`
- `string_literal ::= '"' <characters> '"'`
- `donor ::= '<' TYPE_ID '>'`
- `acceptor ::= '<<' TYPE_ID '>>'`
- `primitive_call ::= '(' arg* ( '!' | '?' | '' ) IDENTIFIER ')'`
- `arg ::= INTEGER ; positive (1, 2, 3, ...)`
- `PRIMITIVE_NAME ::= e.g. SAME, CONCAT, PLUS, MINUS, RNDINT, ...`
- `TYPE_ID ::= non-empty string (implementation may allow quoted ids)`
- Tokens must be separated by whitespace/newline. String literal content is opaque.

### 3) Node Outputs and Types
Every node provides:
- `.str` — string value output (always present). 
  - `null` means error.
  - `"F"` means false.
  - `"T"` means true.

- **String literal:** `.str = literal contents`
- **Primitive nodes:** Compute `.str` per their own semantics.
- **Donor/acceptor:** These are placeholders (see section 6).

### 4) Reference Resolution
- When evaluating a `primitive_call` at position $p$, each numeric argument $n$ refers to the node at position $(p - n)$.
- **Argument order:** The numeric args, read left-to-right, correspond to primitive parameters $1..k$.
- **Error conditions:**
  - Error if any referenced node position $< 1$.
  - Error if arity does not match the primitive's required number of args.

### 5) Base Primitives
General rule: Primitives that transform data typically read their inputs from `argX.str`. Logical primitives usually set `.str` based on `.str` equality/tests. On parse/compute failures, `.str` is set to `null`.

- **!RNDINT**
  - Syntax: `( RNDINT )`
  - Arity: 0
  - Behavior: Generates a random integer (implementation-specified range).
  - Output: `.str = decimal string of the integer`.

- **!CONCAT**
  - Syntax: `( arg1 arg2 CONCAT )`
  - Arity: 2
  - Inputs: `arg1.str`, `arg2.str`
  - Output: `.str = arg1.str + arg2.str`.

- **!SAME**
  - Syntax: `( arg1 arg2 SAME )`
  - Arity: 2
  - Inputs: `arg1.str`, `arg2.str`
  - Output: `"T"` if `(arg1.str == arg2.str)`; `"F"` if `(arg1.str != arg2.str)`.

- **!PLUS**
  - Syntax: `( arg1 arg2 PLUS )`
  - Arity: 2
  - Inputs: `argN.str` parsed as numbers (integer or floating; implementation choice).
  - Output: If parse succeeds, `.str = string(formal sum)`; if parse fails, `.str = null`.

- **!MINUS**
  - Syntax and behavior analogous to `PLUS`; result = `arg1 - arg2`.

#### 5.1 Error Propagation (The "Null Cascade" Rule)
To ensure stability and predictability, the language employs a strict error propagation model.

**The Rule:** A primitive is "broken" if any of its required inputs are in an error state. If a primitive is broken, it must not execute its internal logic and must immediately output `null`.

**Formal Logic:** For any primitive $P$ with arguments $\{arg_1, arg_2, \dots, arg_n\}$:
- If $\exists i \in \{1 \dots n\}$ such that $arg_i.str = \text{null}$, then $P.str = \text{null}$.

**Implementation Guidelines:**
1. **Pre-condition Check:** Every primitive must check the `.str` value of its resolved references *before* attempting to parse or process the data.
2. **No Recovery:** A `null` value cannot be "fixed" or converted back into a valid string by a subsequent primitive. Once a node is `null`, all downstream nodes that reference it will also become `null`.
3. **Strictness:** This applies even if multiple inputs are `null`. For example, in the `SAME` primitive, `(null null SAME)` does **not** return `"T"`; it returns `null`.

**Example Trace:** ` "Apple" (RNDINT_FAIL) (1 2 PLUS) (3 1 CONCAT)`
- Node 1: `"Apple"` $\rightarrow$ `.str = "Apple"`
- Node 2: `(RNDINT_FAIL)` $\rightarrow$ `.str = null`
- Node 3: `(1 2 !PLUS)` $\rightarrow$ sees Node 2 is `null` $\rightarrow$ Short-circuits $\rightarrow$ `.str = null`
- Node 4: `(3 1 !CONCAT)` $\rightarrow$ sees Node 3 is `null` $\rightarrow$ Short-circuits $\rightarrow$ `.str = null`

### 6) Donor and Acceptor (Merge Placeholders)

**Representation:**
- **Acceptor:** `<<TYPE_ID>>`
- **Donor:** `<TYPE_ID>`

**Semantics:**
Placeholders intended for merging subgraphs. They act as no-op nodes at runtime: they pass through the `.str` value of the immediately preceding node. If a placeholder is the first node in a sequence, its output is an empty string.

**Merge Algorithm:**
A merge takes a **Donor Graph** ($G_{don}$) and an **Acceptor Graph** ($G_{acc}$) and produces a new **Result Graph** ($G_{res}$).

1.  **Validation:**
    - Locate the first `Donor` node ($D$) in $G_{don}$ and the first `Acceptor` node ($A$) in $G_{acc}$.
    - If either is missing, the merge fails (`RuntimeException`).
    - If `D.TYPE_ID` $\neq$ `A.TYPE_ID`, the merge fails (`RuntimeException`).

2.  **Prefix Extraction:**
    - Define the **Donor Prefix** as the ordered sequence of all nodes in $G_{don}$ from index $0$ up to and including $D$.

3.  **Graph Assembly:**
    - $G_{res}$ is constructed by concatenating nodes in the following order:
        1. All nodes in $G_{acc}$ appearing **before** $A$.
        2. All nodes in the **Donor Prefix**.
        3. All nodes in $G_{acc}$ appearing **after** $A$.
    - *Note: The Acceptor node $A$ is effectively replaced by the Donor node $D$.*

4.  **Relative Reference Adjustment:**
    Nodes within the Donor Prefix retain their original relative references. However, for every node $N$ in $G_{res}$ that originated from $G_{acc}$ and appears **after** the merge point, its relative arguments ($arg$) must be updated.
    
    Let $\text{offset} = \text{length}(\text{Donor Prefix}) - 1$.
    For each argument $arg$ in node $N$ (where $N$ is at index $i$ in the original $G_{acc}$):
    - Calculate the original target index: $\text{target} = i - arg$.
    - **Case A: Target was before the Acceptor** ($\text{target} < \text{index}(A)$):
        The distance to the target has increased. Update: $arg_{new} = arg + \text{offset}$.
    - **Case B: Target was the Acceptor or after the Acceptor** ($\text{target} \geq \text{index}(A)$):
        The distance remains the same because the target and the current node were both shifted forward by the same amount, or the target (the Acceptor) was replaced by the Donor at the same relative position. Update: $arg_{new} = arg$.

**Example of Index Adjustment:**
If the Donor Prefix has 3 nodes and we replace the Acceptor:
- A reference to a node *before* the acceptor moves from distance 2 $\rightarrow$ 4 (offset of 2).
- A reference to the acceptor itself stays distance 1 $\rightarrow$ 1 (now pointing to the Donor).
- A reference to a node *after* the acceptor stays distance 1 $\rightarrow$ 1.
### 7) Evaluation Model
- Single left-to-right pass (parse/resolve/evaluate).
- When encountering a primitive, resolve each numeric arg to the corresponding previous node and use the required outputs (`.str`) per primitive semantics.
- The final program result is the last node's output (`.str`) unless the embedding system specifies otherwise.
- Random primitives (`RNDINT`) are implementation-dependent regarding determinism (seedable or not).

### 8) Errors and Validation
- **Syntax errors:** Malformed tokens, unclosed quotes, missing parentheses.
- **Reference errors:** Numeric arg points before the start of the program (out-of-range).
- **Arity errors:** Wrong number of args for a primitive.
- **Merge errors:** `TYPE_ID` mismatch, invalid donor prefix, or attempted merge that would create cycles.
- **Runtime errors:** Numeric parse failure for `PLUS`/`MINUS` (set `.str = null`).

### 9) Examples
- **Mutual Relations:**
  - `"Sarah" "John" (1 2 LOVES)`
    - Node 1: `"Sarah"` (.str="Sarah")
    - Node 2: `"John"` (.str="John")
    - Node 3: `(1 2 LOVES)` $\rightarrow$ args: 1 $\rightarrow$ Node 2, 2 $\rightarrow$ Node 1 $\rightarrow$ LOVES(John, Sarah)
- **Reciprocal Relations:**
  - `"Sarah" "John" (1 2 LOVES) (3 2 LOVES)`
    - Two separate LOVES nodes model mutual liking without graph cycles.
- **Numeric/Random:**
  - `(!RNDINT) (!RNDINT) (1 2 !PLUS) (1 3 !PLUS) (1 2 !MINUS) (5 1 !SAME)`
    - Relative indices are resolved strictly at each primitive's position.
- **String Building:**
  - `"tehén" "fej" (2 1 !CONCAT) "en" (2 1 !CONCAT)`
    - Node 1: `"tehén"`
    - Node 2: `"fej"`
    - Node 3: `(2 1 !CONCAT)` $\rightarrow$ !CONCAT(Node 1, Node 2) = `"tehénfej"`
    - Node 4: `"en"`
    - Node 5: `(2 1 !CONCAT)` $\rightarrow$ !CONCAT(Node 3, Node 4) = `"tehénfejen"`
- **Value-Based Identity & Directedness:**
  `"Sarah" "John" (1 2 LOVES) "Sarah" (1 2 ?LOVES)`
- Node 1: `"Sarah"`
- Node 2: `"John"`
- Node 3: `(1 2 LOVES)` $\rightarrow$ Registry adds `("John", "Sarah", "LOVES")`. Output: `"T"`.
- Node 4: `"Sarah"` (A new node, but has the same value as Node 1).
- Node 5: `(1 2 ?LOVES)` $\rightarrow$ Relative to pos 5, this checks Node 4 (`"Sarah"`) and Node 3 (`"T"`).
- *Correction for the Query:* To check if John loves Sarah using the new Sarah node:
  `"Sarah" "John" (1 2 LOVES) "Sarah" (2 1 ?LOVES)`
  - Node 5: `(2 1 ?LOVES)` $\rightarrow$ Checks Node 3 (`"T"`) and Node 4 (`"Sarah"`).
- *To check the original entities via the new Sarah node:*
  `"Sarah" "John" (1 2 LOVES) "Sarah" (3 1 ?LOVES)`
  - Node 5: `(3 1 ?LOVES)` $\rightarrow$ Checks Node 2 (`"John"`) and Node 4 (`"Sarah"`). 
  - Registry contains `("John", "Sarah", "LOVES")`. Result: `"T"`.
