package node.primitive;

import node.Node;
import java.util.List;

public class Minus implements Primitive {
    @Override public int getArity() { return 2; }
    @Override public String execute(List<Node> inputs) {
        try {
            double a = Double.parseDouble(inputs.get(0).getStr());
            double b = Double.parseDouble(inputs.get(1).getStr());
            return String.valueOf((int)a - (int)b);
        } catch (Exception e) { return null; }
    }
}