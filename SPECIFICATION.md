# Specification — Position-based Textual DAG Description Language

**Summary:** A linear text sequence describes a directed acyclic graph (DAG). Each token is a node: a string literal, a primitive/function call, or a donor/acceptor placeholder. Primitive calls use positional, relative references to earlier nodes: 1 = the immediately preceding node, 2 = the one before that, etc. Numbers listed left-to-right map to primitive parameter order. Every node exposes one output: a string (`.str`). String literals are quoted.

### 1) Concepts and High-Level Rules
- The program is a sequence of nodes processed left-to-right. Node indices increase with position.
- All relative references point backward only; this guarantees acyclicity.
- A primitive call is written in parentheses; inside are integer arguments (relative references) followed by the primitive name.
- **The Global Connection Registry:** A persistent store of directed facts. Unlike Computational primitives which transform data, Connection and Query primitives interact with this registry using **Value-based Identity**.
- **Value-based Identity:** A node's identity is its `.str` value. If two nodes have the same `.str` value, they are treated as the same entity in the Registry.
- **Primitive Classification:**
    - **Computation (`!NAME`):** Reads `.str` values $\rightarrow$ Produces a new `.str` value.
    - **Connection (`NAME`):** Reads `.str` values $\rightarrow$ Records a directed fact `(Source, Target, Relation)` in the Registry $\rightarrow$ **Produces the fact string as output** (e.g., `"(John LOVES Sarah)"`).
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
- `TYPE_ID ::= non-empty string`
- Tokens must be separated by whitespace/newline.

### 3) Node Outputs and Types
Every node provides:
- `.str` — string value output (always present).
    - `null` means error.
    - `"F"` means false.
    - `"T"` means true.

- **String literal:** `.str = literal contents`
- **Primitive nodes:** Compute `.str` per their own semantics.
- **Donor/acceptor:** Placeholders that inherit the `.str` value of the preceding node.

### 4) Reference Resolution
- When evaluating a `primitive_call` at position $p$, each numeric argument $n$ refers to the node at position $(p - n)$.
- **Argument order:** The numeric args, read left-to-right, correspond to primitive parameters $1..k$.
- **Error conditions:** Error if any referenced node position $< 0$ or if arity does not match.

### 5) Base Primitives
On parse/compute failures, `.str` is set to `null`.

- **`!RNDINT`**: (Arity 0) $\rightarrow$ `.str = random integer string`.
- **`!CONCAT`**: (Arity 2) $\rightarrow$ `.str = arg1.str + arg2.str`.
- **`!SAME`**: (Arity 2) $\rightarrow$ `"T"` if equal; `"F"` if not.
- **`!PLUS`**: (Arity 2) $\rightarrow$ `.str = numeric sum`.
- **`!MINUS`**: (Arity 2) $\rightarrow$ `.str = numeric difference`.

#### 5.1 Error Propagation (The "Null Cascade" Rule)
A primitive is "broken" if any of its required inputs are in an error state.
- **The Rule:** If $\exists i$ such that $arg_i.str = \text{null}$, then $P.str = \text{null}$.
- Once a node is `null`, all downstream nodes that reference it also become `null`.

### 6) Donor and Acceptor (Merge Placeholders)

**Representation:**
- **Acceptor:** `<<TYPE_ID>>`
- **Donor:** `<TYPE_ID>`

**Semantics:**
These act as no-op nodes at runtime: they pass through the `.str` value of the immediately preceding node. If a placeholder is the first node in a sequence, its output is an empty string.

**Merge Algorithm:**
A merge takes a **Donor Graph** ($G_{don}$) and an **Acceptor Graph** ($G_{acc}$) and produces a **Result Graph** ($G_{res}$).

1. **Validation:**
    - Locate the first `Donor` node ($D$) in $G_{don}$ and the first `Acceptor` ($A$) node that has the same TYPE_ID as ($D$) in $G_{acc}$.
    - If either is missing the merge fails (`RuntimeException`).

2. **Prefix Extraction:**
    - Define the **Donor Prefix** as the ordered sequence of all nodes in $G_{don}$ appearing **before** the Donor marker $D$. (The marker $D$ itself is excluded).

3. **Graph Assembly:**
    - $G_{res}$ is constructed by concatenating nodes in this order:
        1. All nodes in $G_{acc}$ appearing **before** $A$.
        2. All nodes in the **Donor Prefix**.
        3. All nodes in $G_{acc}$ appearing **after** $A$.
    - *Note: The Acceptor node $A$ and Donor node $D$ are both removed from the final graph.*

4. **Reference Redirection:**
    - To maintain graph integrity, any node in $G_{res}$ that originated from $G_{acc}$ and previously held a reference to the `Acceptor` node ($A$) must be updated.
    - **The Rule:** These references are redirected to point to the node that immediately preceded the `Donor` marker $D$ in the original Donor Graph.
    - All other references (those not pointing to $A$) remain unchanged.

### 7) Evaluation Model
- Single left-to-right pass.
- The final program result is the last node's output `.str`.

### 8) Errors and Validation
- **Syntax errors:** Malformed tokens, unclosed quotes, missing parentheses.
- **Reference errors:** Numeric arg points before the start of the program.
- **Arity errors:** Wrong number of args for a primitive.
- **Merge errors:** `TYPE_ID` mismatch.

### 9) Examples
- **Connection & Query:**
  `"Sarah" "John" (1 2 LOVES) "Sarah" (3 1 ?LOVES)`
  - Node 1: `"Sarah"`
  - Node 2: `"John"`
  - Node 3: `(1 2 LOVES)` $\rightarrow$ Registry adds `("John", "Sarah", "LOVES")`. **Output: `"(John LOVES Sarah)"`**.
  - Node 4: `"Sarah"`
  - Node 5: `(3 1 ?LOVES)` $\rightarrow$ Checks if Node 2 (`"John"`) loves Node 4 (`"Sarah"`). **Result: `"T"`**.

- **String Building:**
  `"tehén" "fej" (2 1 !CONCAT) "en" (2 1 !CONCAT)`
  - Result: `"tehénfejen"`