package node;

import java.util.List;
import java.util.Map;

public class StringLiteralNode extends Node {
    private final String value;

    public StringLiteralNode(String value) {
        this.value = value;
    }

    @Override
    public NodeOutput evaluate(List<NodeOutput> history, Map<String, PrimitiveOperation> primitives) {
        return null; // Implementation hidden
    }
}