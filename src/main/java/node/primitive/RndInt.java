package node.primitive;

import node.Node;
import java.util.List;
import java.util.Random;

public class RndInt implements Primitive {
    @Override public int getArity() { return 0; }
    @Override public String execute(List<Node> inputs) {
        return String.valueOf(new Random().nextInt(1000));
    }
}