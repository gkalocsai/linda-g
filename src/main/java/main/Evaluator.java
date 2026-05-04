package main;

import java.util.HashMap;
import java.util.Map;

import node.Output;
import node.PrimitiveOperation;

class Evaluator {
    private final Map<String, PrimitiveOperation> primitiveRegistry;

    public Evaluator() {
        this.primitiveRegistry = new HashMap<>();
        initializePrimitives();
    }

    private void initializePrimitives() {
        // Logic to map "PLUS" -> new PlusOperation(), etc.
    }

    public Output evaluate(DAGProgram program)  {
        return null; // Implementation hidden
    }
}