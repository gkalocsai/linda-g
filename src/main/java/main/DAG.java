package main;

import node.Node;
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
}