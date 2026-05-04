package node;

import java.util.List;
import java.util.Map;



public class PrimitiveCall extends Node {
    private final String primitiveName;
    private final int[] relativeArgs;

    public PrimitiveCall(String primitiveName, int[] relativeArgs) {
        this.primitiveName = primitiveName;
        this.relativeArgs = relativeArgs;
    }

    @Override
    public Output evaluate(List<Output> history, Map<String, PrimitiveOperation> primitives) {
        return null; // Implementation hidden
    }
}