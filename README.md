# Position-based Textual DAG Language

A Java implementation of a linear, position-based description language for Directed Acyclic Graphs (DAGs). This language allows for the definition of data pipelines and logical relations using relative backward references and subgraph merging via Donor/Acceptor placeholders.

## 📌 Overview

In this language, a program is a sequence of nodes processed from left to right. Each node produces two outputs: a string (`.str`) and a boolean (`.bool`). 

The core strength of the language is its **Relative Reference System**: instead of using absolute IDs, primitive calls refer to previous nodes by their relative distance from the current position.
- `1` refers to the immediately preceding node.
- `2` refers to the node before that, and so on.

This backward-only referencing system mathematically guarantees that the resulting graph is acyclic.

## 🛠 Language Specification

### 1. Node Types
| Node Type | Syntax | `.str` Output | `.bool` Output |
| :--- | :--- | :--- | :--- |
| **String Literal** | `"text"` | The literal text | Always `true` |
| **Primitive** | `(arg1 arg2 NAME)` | Result of operation | Result of logic/success |
| **Donor** | `<TYPE_ID>` | Passthrough/Empty | Passthrough/False |
| **Acceptor** | `<<TYPE_ID>>` | Passthrough/Empty | Passthrough/False |

### 2. Base Primitives
The following primitives are supported:
- `RNDINT`: Generates a random integer.
- `CONCAT (arg1, arg2)`: Concatenates two strings.
- `SAME (arg1, arg2)`: Returns `true` if strings are equal.
- `PLUS (arg1, arg2)`: Sums two numeric strings.
- `MINUS (arg1, arg2)`: Subtracts two numeric strings.

### 3. Graph Merging (Donor & Acceptor)
The language supports modularity by allowing one graph to be spliced into another:
1. **The Donor Graph** must end with a `Donor` node (`<ID>`).
2. **The Acceptor Graph** must contain an `Acceptor` node (`<<ID>>`) with a matching ID.
3. **Merge Operation**: The body of the donor graph (all nodes preceding the Donor marker) is injected into the acceptor graph, replacing the Acceptor marker. Both markers are removed from the final resulting graph.

## 💻 Java Implementation

### Class Structure
- `Node`: The abstract base class for all graph elements.
- `Output`: A data object containing the `.bool` and `.str` results.
- `Donor` / `Acceptor`: Placeholder nodes used for graph composition.
- `GraphMerger`: The utility class responsible for splicing donor graphs into acceptor graphs.

### Evaluation Model
Evaluation will be performed in a single linear pass. As each node is evaluated, its output is added to a history list, which subsequent nodes use to resolve their relative references.

```java
List<Output> history = new ArrayList<>();
for (Node node : program) {
    Output out = node.evaluate(history, primitives);
    history.add(out);
}
```

// The final result of the program is the last element in the history list.
## 🚀 Examples

### String Building
**Input:** `"tehén" "fej" (2 1 CONCAT) "en" (2 1 CONCAT)`
1. `"tehén"` $\rightarrow$ `Output(true, "tehén")`
2. `"fej"` $\rightarrow$ `Output(true, "fej")`
3. `(2 1 CONCAT)` $\rightarrow$ `CONCAT("tehén", "fej")` $\rightarrow$ `Output(true, "tehénfej")`
4. `"en"` $\rightarrow$ `Output(true, "en")`
5. `(2 1 CONCAT)` $\rightarrow$ `CONCAT("tehénfej", "en")` $\rightarrow$ `Output(true, "tehénfejen")`

### Subgraph Merging
**Donor Graph:** `"Hello" <GREET>`  
**Acceptor Graph:** `"World" <<GREET>> (2 1 CONCAT)`

**Merged Result:** `"World" "Hello" (2 1 CONCAT)`  
*(The `<<GREET>>` and `<GREET>` markers are removed, and the donor's body is spliced in. The CONCAT primitive now references "Hello" and "World".)*
