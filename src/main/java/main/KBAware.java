package main;

import java.util.List;

import node.Node;

public interface KBAware {
    String executeWithKB(List<Node> inputs, KnowledgeBase kb);
}