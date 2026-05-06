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
            resultNodes.add(accNodes.get(i));
        }

        // 3. Insert the donor prefix nodes
        resultNodes.addAll(donorPrefix);

        // 4. Add nodes from the acceptor graph that come AFTER the acceptor
        // We calculate the offset: how many nodes were added minus the one we replaced
        int offset = donorPrefix.size() - 1;

        for (int i = acceptorIdx + 1; i < accNodes.size(); i++) {
            Node n = accNodes.get(i);
            if (n instanceof PrimitiveCall) {
                PrimitiveCall pc = (PrimitiveCall) n;
                List<Integer> newArgs = new ArrayList<>();
                
                for (int arg : pc.getArgs()) {
                    // Calculate the absolute index this argument pointed to in the original acceptor graph
                    int originalTargetIdx = i - arg;
                    
                    if (originalTargetIdx < acceptorIdx) {
                        // If it pointed to something BEFORE the acceptor, the distance has increased
                        newArgs.add(arg + offset);
                    } else {
                        // If it pointed to the acceptor itself, the distance is the same 
                        // because the acceptor was replaced by the donor node at the same relative slot.
                        newArgs.add(arg);
                    }
                }
                // Create a new node instance to keep the original DAG immutable
                resultNodes.add(new PrimitiveCall(newArgs, pc.getMode(), pc.getName()));
            } else {
                resultNodes.add(n);
            }
        }

        DAG resultDag = new DAG();
        resultNodes.forEach(resultDag::addNode);
        return resultDag;
    }

    private static Node findDonor(DAG dag) {
        return dag.getNodes().stream().filter(n -> n instanceof Donor).findFirst().orElse(null);
    }

    private static Node findAcceptor(DAG dag) {
        return dag.getNodes().stream().filter(n -> n instanceof Acceptor).findFirst().orElse(null);
    }
}