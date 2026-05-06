package node.primitive;

import node.Node;
import java.util.List;
import java.util.Objects;

public class Same implements Primitive {
    @Override public int getArity() { return 2; }
    @Override public String execute(List<Node> inputs) {
        return Objects.equals(inputs.get(0).getStr(), inputs.get(1).getStr()) ? "T" : "F";
    }
}