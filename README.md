# Position-based Textual DAG Language

A Java implementation of a linear, position-based description language for Directed Acyclic Graphs (DAGs). This language allows for the definition of data pipelines and logical relations using relative backward references and subgraph merging via Donor/Acceptor placeholders.

## 📌 Overview

In this language, a program is a sequence of nodes processed from left to right. Each node produces a single string result. 

The core strength of the language is its **Relative Reference System**: instead of using absolute IDs, primitive calls refer to previous nodes by their relative distance from the current position.
- `1` refers to the immediately preceding node.
- `2` refers to the node before that, and so on.

This backward-only referencing system mathematically guarantees that the resulting graph is acyclic.

## 🛠 Language Specification

### 1. Node Types

| Node Type | Syntax | Description |
| :--- | :--- | :--- |
| **String Literal** | `"text"` | A constant string value. |
| **Primitive Call** | `(rel1 rel2 MODE NAME)` | An operation that consumes previous nodes and produces a result. |
| **Donor** | `<TYPE_ID>` | A marker used to export a subgraph for merging. |
| **Acceptor** | `<<TYPE_ID>>` | A placeholder that can be replaced by a matching Donor subgraph. |


### 2. Primitive Execution Modes

Unlike standard functions, primitives in this language operate in three distinct modes determined by a prefix character:

*   **Fact Creation (Default/No Prefix):** `(1 2 NAME)`
    *   Creates a "fact" in the global registry in the format: `(value1 NAME value2)`.
    *   The result of the node is the fact string itself.
*   **Query Mode (`?`):** `(1 2 ?NAME)`
    *   Checks if the fact `(value1 NAME value2)` exists in the registry.
    *   Returns `"T"` if it exists, `"F"` otherwise.
*   **Execution Mode (`!`):** `(1 2 !NAME)`
    *   Performs a functional computation using the logic defined in the `Primitive` class.
    *   Example: `!PLUS` will return the numeric sum of the two inputs.

### 3. Base Primitives

The following computations are available in **Execution Mode (`!`)**:

*   `RNDINT`: Generates a random integer.
*   `CONCAT`: Concatenates two strings.
*   `SAME`: Returns `"T"` if strings are equal, `"F"` otherwise.
*   `PLUS`: Sums two numeric strings.
*   `MINUS`: Subtracts two numeric strings.

### 4. Graph Merging (Donor & Acceptor)

The language allows one graph to be spliced into another based on matching `TYPE_ID`s:

1.  **Identification**: The `GraphMerger` finds the first `Donor` in the donor graph and the first matching `Acceptor` in the acceptor graph.
2.  **The Replacement**: The `Acceptor` node is removed. It is replaced by the **body of the donor graph** (all nodes appearing before the Donor marker).
3.  **Reference Update**: Any node in the acceptor graph that previously referenced the `Acceptor` node is updated to reference the node that immediately preceded the `Donor` marker.
4.  **Cleanup**: Both the `Donor` and `Acceptor` markers are removed from the final DAG.

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
