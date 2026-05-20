package main;

import node.*;
import node.primitive.*;
import java.util.*;

public class Evaluator {
    private final KnowledgeBase kb = new KnowledgeBase();
    private final Map<String, Primitive> computations = new HashMap<>();

    public Evaluator() {
        computations.put("RNDINT", new RndInt());
        computations.put("CONCAT", new Concat());
        computations.put("SAME", new Same());
        computations.put("PLUS", new Plus());
        computations.put("MINUS", new Minus());
        computations.put("NEW", new CreateThing());
        computations.put("FIND", new Find()); // Added FIND as a real primitive
    }

    public String evaluate(DAG dag) {
        List<Node> nodes = dag.getNodes();
        for (Node node : nodes) {
            if (node instanceof PrimitiveCall) evaluatePrimitive((PrimitiveCall) node);
            else if (node instanceof PlaceHolder) evaluatePlaceHolder((PlaceHolder) node);
        }
        return nodes.isEmpty() ? null : nodes.get(nodes.size() - 1).getResult();
    }

    private void evaluatePlaceHolder(PlaceHolder ph) {
        Node input = ph.getInput();
        ph.setResult(input == null ? "" : input.getResult());
    }

    private void evaluatePrimitive(PrimitiveCall call) {
        List<Node> resolved = call.getArgs();
        for (Node n : resolved) {
            if (n.getResult() == null) { call.setResult(null); return; }
        }

        char mode = call.getMode();
        String name = call.getName();

        if (mode == '!') {
            Primitive p = computations.get(name);
            if (p == null || resolved.size() != p.getArity()) {
                call.setResult(null);
                return;
            }
            // Pass the knowledge base to the primitive
            if(p instanceof KBAware)
            call.setResult(((KBAware)p).executeWithKB(resolved, kb)); 
            else {
            	call.setResult(p.execute(resolved));
            }
        } 
        else if (mode == '?') {
            if (resolved.size() != 2) { call.setResult(null); return; }
            boolean exists = kb.verify(resolved.get(0).getResult(), name, resolved.get(1).getResult());
            call.setResult(exists ? "@TRUE@" : "@FALSE@");
        } 
        else {
            if (resolved.size() != 2) { call.setResult(null); return; }
            String s = resolved.get(0).getResult();
            String o = resolved.get(1).getResult();
            kb.addTriple(s, name, o);
            call.setResult("(" + s + " " + name + " " + o + ")");
        }
    }
}