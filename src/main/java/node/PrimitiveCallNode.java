package node;

import java.util.List;
import java.util.Map;



public class PrimitiveCallNode extends Node {
    private final String primitiveName;
    private final int[] relativeArgs;

    public PrimitiveCallNode(String primitiveName, int[] relativeArgs) {
        this.primitiveName = primitiveName;
        this.relativeArgs = relativeArgs;
    }

    @Override
    public NodeOutput evaluate(List<NodeOutput> history, Map<String, PrimitiveOperation> primitives) {
        return null; // Implementation hidden
    }
}