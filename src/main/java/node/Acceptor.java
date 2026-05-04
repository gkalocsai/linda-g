package node;

import java.util.List;
import java.util.Map;

public class Acceptor extends Node {
    private final String typeId;

    public Acceptor(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeId() {
        return typeId;
    }

    @Override
    public Output evaluate(List<Output> history, Map<String, PrimitiveOperation> primitives) {
        // According to specification: "no-op nodes at runtime: 
        // if they have an input, they copy their input node's outputs; 
        // if not, they produce empty/false outputs"
        if (history == null || history.isEmpty()) {
            return new Output(false, "");
        }

        // Get the output of the immediately preceding node (the "input")
        Output previous = history.get(history.size() - 1);
        
        // Return a copy of the previous output
        return new Output(previous.bool, previous.str);
    }
}