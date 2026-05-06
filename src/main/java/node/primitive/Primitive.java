package node.primitive;

import node.Node;
import java.util.List;

public interface Primitive {
    String execute(List<Node> inputs);
    int getArity();
}