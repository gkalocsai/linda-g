package main;

import node.*;
import node.primitive.*;
import java.util.*;

public class Evaluator {
    private final Set<String> registry = new HashSet<>();
    private final Map<String, Primitive> computations = new HashMap<>();

    public Evaluator() {
        computations.put("RNDINT", new RndInt());
        computations.put("CONCAT", new Concat());
        computations.put("SAME", new Same());
        computations.put("PLUS", new Plus());
        computations.put("MINUS", new Minus());
    }

    public String evaluate(DAG dag) {
        List<Node> nodes = dag.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node instanceof PrimitiveCall) {
                evaluatePrimitive((PrimitiveCall) node, i, nodes);
            } else if (node instanceof PlaceHolder) {
                // Placeholders pass through the value of the previous node
                if (i > 0) node.setResult(nodes.get(i - 1).getResult());
                else node.setResult("");
            }
        }
        return nodes.isEmpty() ? null : nodes.get(nodes.size() - 1).getResult();
    }

    private void evaluatePrimitive(PrimitiveCall call, int pos, List<Node> nodes) {
        List<Node> resolved = new ArrayList<>();
        for (int rel : call.getArgs()) {
            int targetIdx = pos - rel;
            if (targetIdx < 0 || targetIdx >= nodes.size()) {
                call.setResult(null); return;
            }
            resolved.add(nodes.get(targetIdx));
        }

        // Null Cascade Rule
        for (Node n : resolved) {
            if (n.getResult() == null) {
                call.setResult(null); return;
            }
        }

        char mode = call.getMode();
        String name = call.getName();

        if (mode == '!') {
            Primitive p = computations.get(name);
            if (p == null || resolved.size() != p.getArity()) { call.setResult(null); return; }
            call.setResult(p.execute(resolved));
        } else if (mode == '?') {
            if (resolved.size() != 2) { call.setResult(null); return; }
            String key = resolved.get(0).getResult() + "|" + name + "|" + resolved.get(1).getResult();
            call.setResult(registry.contains(key) ? "T" : "F");
        } else {
            // --- CONNECTION ---
            if (resolved.size() != 2) { 
                call.setResult(null); 
                return; 
            }
            
            // Reification: Create a wrapped S-Expression.
            // This allows facts to be treated as unique entities for Value-Based Identity.
            String src = resolved.get(0).getResult();
            String tgt = resolved.get(1).getResult();
            String factId = "(" + src + " " + name + " " + tgt + ")";
            
            registry.add(factId);
            
            // Return the factId so this node can be used as an argument for another primitive.
            call.setResult(factId); 
        }
    }
}