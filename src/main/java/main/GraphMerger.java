package main;
import node.*;
import java.util.*;

public class GraphMerger {
    public static DAG merge(DAG donorGraph, DAG acceptorGraph) {
        Node donorNode = findDonor(donorGraph);
        Node acceptorNode = findAcceptor(acceptorGraph);
        if (donorNode == null || acceptorNode == null) {
            throw new RuntimeException("Merge markers missing");
        }
        if (!((Donor) donorNode).getTypeId().equals(((Acceptor) acceptorNode).getTypeId())) {
            throw new RuntimeException("Type ID mismatch");
        }

        int donorIdx = donorGraph.getNodes().indexOf(donorNode);
        int acceptorIdx = acceptorGraph.getNodes().indexOf(acceptorNode);

        // 1. Extract the donor prefix (from start up to and including the donor node)
        List<Node> donorPrefix = new ArrayList<>(donorGraph.getNodes().subList(0, donorIdx + 1));
        List<Node> accNodes = acceptorGraph.getNodes();
        List<Node> resultNodes = new ArrayList<>();

        // 2. Add nodes from the acceptor graph that come BEFORE the acceptor
        for (int i = 0; i < acceptorIdx; i++) {
            resultNodes.add(processNode(accNodes.get(i), acceptorNode, donorNode));
        }

        // 3. Insert the donor prefix nodes
        resultNodes.addAll(donorPrefix);

        // 4. Add nodes from the acceptor graph that come AFTER the acceptor
        for (int i = acceptorIdx + 1; i < accNodes.size(); i++) {
            resultNodes.add(processNode(accNodes.get(i), acceptorNode, donorNode));
        }

        DAG resultDag = new DAG();
        resultNodes.forEach(resultDag::addNode);
        return resultDag;
    }

    /**
     * Ensures that if a node is a PrimitiveCall, any reference to the 
     * old Acceptor node is replaced by the new Donor node.
     */
    private static Node processNode(Node n, Node acceptorNode, Node donorNode) {
        if (n instanceof PrimitiveCall) {
            PrimitiveCall pc = (PrimitiveCall) n;
            List<Node> oldArgs = pc.getArgs();
            List<Node> newArgs = new ArrayList<>();
            
            boolean needsReplacement = false;
            for (Node arg : oldArgs) {
                if (arg == acceptorNode) {
                    newArgs.add(donorNode);
                    needsReplacement = true;
                } else {
                    newArgs.add(arg);
                }
            }
            
            // Only create a new object if a reference was actually swapped
            return needsReplacement ? new PrimitiveCall(newArgs, pc.getMode(), pc.getName()) : n;
        }
        return n;
    }

    private static Node findDonor(DAG dag) {
        return dag.getNodes().stream().filter(n -> n instanceof Donor).findFirst().orElse(null);
    }

    private static Node findAcceptor(DAG dag) {
        return dag.getNodes().stream().filter(n -> n instanceof Acceptor).findFirst().orElse(null);
    }
}