package node.primitive;

import node.Node;
import java.util.List;

public class Concat extends Primitive {
    @Override public int getArity() { return 2; }
    @Override public String execute(List<Node> inputs) {
        return inputs.get(0).getResult() + inputs.get(1).getResult();
    }
	
}