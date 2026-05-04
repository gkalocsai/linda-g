package node;

import java.util.List;
import java.util.Map;

public abstract class Node {
    public abstract Output evaluate(List<Output> history, Map<String, PrimitiveOperation> primitives);
}