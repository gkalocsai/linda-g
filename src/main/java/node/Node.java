package node;

import java.util.List;
import java.util.Map;

public abstract class Node {
    public abstract NodeOutput evaluate(List<NodeOutput> history, Map<String, PrimitiveOperation> primitives);
}