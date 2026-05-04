package main;

import java.util.List;
import java.util.Map;

import node.Node;
import node.NodeOutput;
import node.PrimitiveOperation;

class DonorNode extends Node {
    private final String typeId;

    public DonorNode(String typeId) {
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