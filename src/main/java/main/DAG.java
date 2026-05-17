package main;

import node.Node;
import node.PrimitiveCall;

import java.util.ArrayList;
import java.util.List;

public class DAG {
    private final List<Node> nodes = new ArrayList<>();

    public void addNode(Node node) { nodes.add(node); }
    public List<Node> getNodes() { return nodes; }
    public void setNodes(List<Node> nodes) {
        this.nodes.clear();
        this.nodes.addAll(nodes);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<Node> nodeList = getNodes();

        for (int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            if (node instanceof PrimitiveCall) {
                PrimitiveCall pc = (PrimitiveCall) node;
                sb.append("(");                
                for (Node arg : pc.getArgs()) {
                    int argIndex = nodeList.indexOf(arg);
                    if (argIndex == -1) {
                        sb.append(" ERROR_REF"); 
                    } else {                   
                        sb.append(" ").append(i - argIndex);
                    }
                }
                char mode = pc.getMode();
                String name = pc.getName();
                if (mode != ' ') {
                    sb.append(" ").append(mode).append(name);
                } else {
                    sb.append(" ").append(name);
                }
                
                sb.append(")");
            } else {
                sb.append(node.toString());
            }
            
            if (i < nodeList.size() - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}