package main;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import node.Acceptor;
import node.Donor;
import node.Node;

public class GraphMerger {

    /**
     * Merges the donor graph into the acceptor graph.
     * Assumption: The donor node is the last element of the donorGraph.
     * The donor and acceptor markers are both removed from the final result.
     * 
     * @param donorGraph    The graph providing the prefix.
     * @param acceptorGraph The graph receiving the prefix.
     * @return A new list of nodes with the donor body spliced into the acceptor graph.
     * @throws MergeException if the donor graph is empty, doesn't end with a Donor, 
     *                        or no matching Acceptor is found.
     */
    public static List<Node> merge(List<Node> donorGraph, List<Node> acceptorGraph) throws MergeException {
        if (donorGraph == null || donorGraph.isEmpty()) {
            throw new MergeException("Donor graph is empty.");
        }

        // 1. Assume the donor is the last node of the graph
        Node lastNode = donorGraph.get(donorGraph.size() - 1);
        if (!(lastNode instanceof Donor)) {
            throw new MergeException("The last node of the donor graph must be a Donor placeholder.");
        }

        // Extract the typeId from the Donor node to find the matching Acceptor
        String typeId = ((Donor) lastNode).getTypeId();

        // 2. Identify the acceptor node A in acceptor graph G_acc using the derived typeId
        int acceptorIdx = findAcceptorIndex(acceptorGraph, typeId);
        if (acceptorIdx == -1) {
            throw new MergeException("No Acceptor node with TYPE_ID '" + typeId + "' found in acceptor graph.");
        }

        // 3. Prepare the Donor Body (the prefix excluding the donor node itself)
        List<Node> donorBody = new ArrayList<>(donorGraph.subList(0, donorGraph.size() - 1));

        // 4. Construct the final graph
        List<Node> mergedGraph = new ArrayList<>();

        // Part A: All nodes in acceptor graph before the acceptor node
        mergedGraph.addAll(acceptorGraph.subList(0, acceptorIdx));

        // Part B: The donor body (the content of the donor graph minus the Donor marker)
        mergedGraph.addAll(donorBody);

        // Part C: All nodes in acceptor graph after the acceptor node
        mergedGraph.addAll(acceptorGraph.subList(acceptorIdx + 1, acceptorGraph.size()));

        return mergedGraph;
    }

    private static int findAcceptorIndex(List<Node> graph, String typeId) {
        return IntStream.range(0, graph.size())
                .filter(i -> graph.get(i) instanceof Acceptor && ((Acceptor) graph.get(i)).getTypeId().equals(typeId))
                .findFirst()
                .orElse(-1);
    }

    public static class MergeException extends Exception {
        public MergeException(String message) {
            super(message);
        }
    }
}