package node;

import java.util.List;
import java.util.Map;

public class AcceptorNode extends Node {
    private final String typeId;

    public AcceptorNode(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeId() {
        return null; // Implementation hidden
    }

    @Override
    public NodeOutput evaluate(List<NodeOutput> history, Map<String, PrimitiveOperation> primitives) {
        return null; // Implementation hidden
    }
}