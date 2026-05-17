package node.primitive;

import node.Node;
import java.util.List;
import java.util.Objects;

public class Same extends Primitive {
    @Override public int getArity() { return 2; }
    @Override public String execute(List<Node> inputs) {
        return Objects.equals(inputs.get(0).getResult(), inputs.get(1).getResult()) ? "T" : "F";
    }
}