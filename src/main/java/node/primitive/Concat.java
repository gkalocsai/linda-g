package node.primitive;

import node.Node;
import java.util.List;

public class Concat implements Primitive {
    @Override public int getArity() { return 2; }
    @Override public String execute(List<Node> inputs) {
        return inputs.get(0).getStr() + inputs.get(1).getStr();
    }
}