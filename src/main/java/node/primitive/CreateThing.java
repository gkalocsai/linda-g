package node.primitive;

import node.Node;
import java.util.List;
import java.util.UUID;

public class CreateThing extends Primitive {
    @Override 
    public int getArity() { 
        return 0; 
    }

    @Override 
    public String execute(List<Node> inputs) {
        // Generates a unique short ID
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return "@" + uniqueId + "@";
    }
}