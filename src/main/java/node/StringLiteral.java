package node;

import java.util.List;
import java.util.Map;

public class StringLiteral extends Node {
    private final String value;

    public StringLiteral(String value) {
        this.value = value;
    }

    @Override
    public Output evaluate(List<Output> history, Map<String, PrimitiveOperation> primitives) {
        return null; // Implementation hidden
    }
}