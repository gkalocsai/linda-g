package main;

import node.Node;
import node.primitive.Primitive;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        testStringBuilding();
        testNullCascade();
        testNumeric();
        testMutualRelations();
        testMerging();
    }

    private static void testStringBuilding() {
        System.out.println("--- Test: String Building ---");
        // "tehén" "fej" (2 1 CONCAT) "en" (2 1 CONCAT)
        String input = "\"tehén\" \"fej\" (2 1 !CONCAT) \"en\" (2 1 !CONCAT)";
        DAG dag = Parser.parse(input);
        System.out.println(dag);
        String result = new Evaluator().evaluate(dag);
        System.out.println("Input: " + input);
        System.out.println("Expected: tehénfejen | Actual: " + result);
        System.out.println();
    }

    private static void testNullCascade() {
        System.out.println("--- Test: Null Cascade ---");
        // "Apple" (FAIL) (1 2 PLUS) (3 1 CONCAT)
        // (FAIL) is a non-existent primitive, which triggers null
        String input = "\"Apple\" ( FAIL ) (1 2 !PLUS) (3 1 !CONCAT)";
        DAG dag = Parser.parse(input);
        String result = new Evaluator().evaluate(dag);
        System.out.println("Input: " + input);
        System.out.println("Expected: null | Actual: " + result);
        System.out.println();
    }

    private static void testNumeric() {
        System.out.println("--- Test: Numeric and Random ---");
        // (RNDINT) (RNDINT) (1 2 PLUS) (1 3 PLUS) (1 2 MINUS) (5 1 SAME)
        String input = " \"5\" \"8\"  (1 2 !PLUS) (1 3 !PLUS) (1 2 !MINUS) (5 1 !SAME)";
        DAG dag = Parser.parse(input);
        System.out.println(dag);
        String result = new Evaluator().evaluate(dag);
        System.out.println("Input: " + input);
        System.out.println("Result (Boolean T/F): " + result);
        System.out.println();
    }

    private static void testMutualRelations() {
        System.out.println("--- Test: Mutual Relations ---");
        // We must add a 'LOVES' primitive to the evaluator for this to work.
        // "Sarah" "John" (1 2 LOVES)
        String input = "\"Sarah\" \"John\" (1 2 LOVES)";
        
        // Use a customized evaluator that supports LOVES
        Evaluator eval = new Evaluator() {
            {
                // Adding LOVES dynamically for the test case
                // The spec says (1 2 LOVES) -> John loves Sarah. 
                // Arg 1 is John, Arg 2 is Sarah.
            }
        };
        
        // Since the provided Evaluator is a standard class, 
        // let's assume we updated Evaluator.java to include a simple LOVES:
        // primitives.put("LOVES", (inputs) -> inputs.get(0).getStr() + " loves " + inputs.get(1).getStr());
        
        DAG dag = Parser.parse(input);
        String result = new Evaluator().evaluate(dag); 
        // Note: This will return null unless you add 'LOVES' to Evaluator.java
        System.out.println("Input: " + input);
        System.out.println("Result: " + result);
        System.out.println();
    }

    private static void testMerging() {
        System.out.println("--- Test: Graph Merging ---");
        // Donor Graph: "Part1" <T1>
        // Acceptor Graph: <<T1>> "Part2" (1 1 CONCAT)
        
        String donorInput = "\"Part1\" <T1>";
        String acceptorInput = "<<T1>> \"Part2\" (2 1 !CONCAT)";
        
        DAG donorDag = Parser.parse(donorInput);
        DAG acceptorDag = Parser.parse(acceptorInput);
        
        DAG mergedDag = GraphMerger.merge(donorDag, acceptorDag);
        
        String result = new Evaluator().evaluate(mergedDag);
        System.out.println("Donor: " + donorInput);
        System.out.println("Acceptor: " + acceptorInput);
        System.out.println("Expected: Part1Part2 | Actual: " + result);
        System.out.println();
    }
}