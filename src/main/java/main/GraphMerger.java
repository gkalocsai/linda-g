package main;

import node.*;
import java.util.*;

public class GraphMerger {
    public static DAG merge(DAG donorGraph, DAG acceptorGraph) {
        // 1. Locate the Donor and the matching Acceptor
        Node donorNode = findDonor(donorGraph);
        if (donorNode == null) throw new RuntimeException("Donor marker missing");

        String donorTypeId = ((Donor) donorNode).getTypeId();
        Node acceptorNode = findAcceptor(acceptorGraph, donorTypeId);
        if (acceptorNode == null) return acceptorGraph;

        // 2. Identify the actual value being donated (the node before the donor marker)
        int donorIdx = donorGraph.getNodes().indexOf(donorNode);
        Node replacementNode = (donorIdx > 0) ? donorGraph.getNodes().get(donorIdx - 1) : null;

        // 3. Build the Replacement Map
        // Maps OldNode -> NewNode to prevent ERROR_REF/Dangling Pointers
        Map<Node, Node> replacementMap = new HashMap<>();
        replacementMap.put(acceptorNode, replacementNode);

        // 4. Process the Acceptor Graph to update references
        List<Node> accNodes = acceptorGraph.getNodes();
        // We'll store the "updated" version of the acceptor nodes here
        List<Node> updatedAcceptorNodes = new ArrayList<>();

        for (Node n : accNodes) {
            if (n instanceof PrimitiveCall) {
                PrimitiveCall pc = (PrimitiveCall) n;
                List<Node> oldArgs = pc.getArgs();
                List<Node> newArgs = new ArrayList<>();
                boolean changed = false;

                for (Node arg : oldArgs) {
                    if (replacementMap.containsKey(arg)) {
                        newArgs.add(replacementMap.get(arg));
                        changed = true;
                    } else {
                        newArgs.add(arg);
                    }
                }

                if (changed) {
                    Node newNode = new PrimitiveCall(newArgs, pc.getMode(), pc.getName());
                    replacementMap.put(n, newNode);
                    updatedAcceptorNodes.add(newNode);
                } else {
                    updatedAcceptorNodes.add(n);
                }
            } else {
                updatedAcceptorNodes.add(n);
            }
        }

        // 5. SLOT INJECTION: Assemble the final node list
        // We iterate through the original acceptor structure.
        // When we hit the Acceptor slot, we drop in the Donor's logic.
        List<Node> finalNodes = new ArrayList<>();
        List<Node> donorPrefix = new ArrayList<>(donorGraph.getNodes().subList(0, donorIdx));
        
        for (int i = 0; i < accNodes.size(); i++) {
            Node originalNode = accNodes.get(i);
            
            if (originalNode == acceptorNode) {
                // INJECT: Replace the acceptor node with the entire donor logic prefix
                finalNodes.addAll(donorPrefix);
            } else {
                // ADD: Add the updated version of the node (from our map/updated list)
                // We find the replacement in the map; if not there, use the updated list
                Node updatedNode = replacementMap.getOrDefault(originalNode, updatedAcceptorNodes.get(i));
                finalNodes.add(updatedNode);
            }
        }

        DAG resultDag = new DAG();
        finalNodes.forEach(resultDag::addNode);
        return resultDag;
    }

    private static Node findDonor(DAG dag) {
        return dag.getNodes().stream().filter(n -> n instanceof Donor).findFirst().orElse(null);
    }

    private static Node findAcceptor(DAG dag, String targetTypeId) {
        return dag.getNodes().stream()
                .filter(n -> n instanceof Acceptor)
                .map(n -> (Acceptor) n)
                .filter(a -> a.getTypeId().equals(targetTypeId))
                .findFirst().orElse(null);
    }
}