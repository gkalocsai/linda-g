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
        for (Node node : nodes) {
            if (node instanceof PrimitiveCall) {
                evaluatePrimitive((PrimitiveCall) node);
            } else if (node instanceof PlaceHolder) {
                evaluatePlaceHolder((PlaceHolder) node);
            }
        }
        return nodes.isEmpty() ? null : nodes.get(nodes.size() - 1).getResult();
    }

    private void evaluatePlaceHolder(PlaceHolder ph) {
        Node input = ph.getInput();
        if (input == null) {
            ph.setResult(""); 
        } else {
            String val = input.getResult();           
            ph.setResult(val); 
        }
    }

    private void evaluatePrimitive(PrimitiveCall call) {
        List<Node> resolved = call.getArgs();
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
            if (resolved.size() != 2) {
                call.setResult(null);
                return;
            }
            String src = resolved.get(0).getResult();
            String tgt = resolved.get(1).getResult();
            String factId = "(" + src + " " + name + " " + tgt + ")";
            registry.add(factId);
            call.setResult(factId);
        }
    }
}