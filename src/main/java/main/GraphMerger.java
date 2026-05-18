package main;
import node.*;
import java.util.*;

public class GraphMerger {
	public static DAG merge(DAG donorGraph, DAG acceptorGraph) {
		// 1. Find the donor node first
		Node donorNode = findDonor(donorGraph);
		if (donorNode == null) {
			throw new RuntimeException("Donor marker missing");
		}

		// 2. Extract the typeId from the donor to use for the search
		String donorTypeId = ((Donor) donorNode).getTypeId();

		// 3. Find the first acceptor node that MATCHES that specific typeId
		Node acceptorNode = findAcceptor(acceptorGraph, donorTypeId);

		//The merge did nothing as no acceptor accepts the donor
		if(acceptorNode == null) return acceptorGraph;

		int donorIdx = donorGraph.getNodes().indexOf(donorNode);
		int acceptorIdx = acceptorGraph.getNodes().indexOf(acceptorNode);

		// 1. Identify the node that actually provides the value (the one before the Donor marker)
		// If the donorNode is the first node, there is no predecessor.
		Node replacementNode = (donorIdx > 0) ? donorGraph.getNodes().get(donorIdx - 1) : null;

		// 2. Extract the donor prefix EXCLUDING the donor node itself
		// subList(0, donorIdx) takes indices 0 to donorIdx-1
		List<Node> donorPrefix = new ArrayList<>(donorGraph.getNodes().subList(0, donorIdx));

		List<Node> accNodes = acceptorGraph.getNodes();
		List<Node> resultNodes = new ArrayList<>();

		// 3. Add nodes from the acceptor graph that come BEFORE the acceptor
		for (int i = 0; i < acceptorIdx; i++) {
			resultNodes.add(processNode(accNodes.get(i), acceptorNode, replacementNode));
		}

		// 4. Insert the donor prefix nodes (the donorNode is now omitted)
		resultNodes.addAll(donorPrefix);

		// 5. Add nodes from the acceptor graph that come AFTER the acceptor
		for (int i = acceptorIdx + 1; i < accNodes.size(); i++) {
			resultNodes.add(processNode(accNodes.get(i), acceptorNode, replacementNode));
		}

		DAG resultDag = new DAG();
		resultNodes.forEach(resultDag::addNode);
		return resultDag;
	}

	/**
	 * Ensures that if a node is a PrimitiveCall, any reference to the
	 * old Acceptor node is replaced by the replacementNode (predecessor of Donor).
	 */
	private static Node processNode(Node n, Node acceptorNode, Node replacementNode) {
		if (n instanceof PrimitiveCall) {
			PrimitiveCall pc = (PrimitiveCall) n;
			List<Node> oldArgs = pc.getArgs();
			List<Node> newArgs = new ArrayList<>();

			boolean needsReplacement = false;
			for (Node arg : oldArgs) {
				if (arg == acceptorNode) {
					newArgs.add(replacementNode);
					needsReplacement = true;
				} else {
					newArgs.add(arg);
				}
			}

			return needsReplacement ? new PrimitiveCall(newArgs, pc.getMode(), pc.getName()) : n;
		}
		return n;
	}

	private static Node findDonor(DAG dag) {
		return dag.getNodes().stream()
				.filter(n -> n instanceof Donor)
				.findFirst()
				.orElse(null);
	}

	private static Node findAcceptor(DAG dag, String targetTypeId) {
		return dag.getNodes().stream()
				.filter(n -> n instanceof Acceptor)
				.map(n -> (Acceptor) n)
				.filter(a -> a.getTypeId().equals(targetTypeId))
				.findFirst()
				.orElse(null);
	}
}