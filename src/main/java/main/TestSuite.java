package main;

import node.*;
import java.util.*;

public class TestSuite {

    public static void main(String[] args) {
        testBasicComputation();
        testNullCascade();
        testKnowledgeRegistry();
        testValueBasedIdentity();
        testComplexMerging();
    }

    private static void assertEquals(String testName, String expected, String actual) {
        if (Objects.equals(expected, actual)) {
            System.out.println("[PASS] " + testName);
        } else {
            System.out.println("[FAIL] " + testName + " | Expected: " + expected + " | Actual: " + actual);
        }
    }

    private static void testBasicComputation() {
        System.out.println("--- Testing Basic Computation ---");
        Evaluator eval = new Evaluator();

        // Test: Plus, Minus, and Same
        // "10" "20" (1 2 !PLUS) "5" (1 3 !MINUS) (1 2 !SAME)
        // 10, 20, 30, (30-5)=25, (25 == 30) = F
        String input = "\"10\" \"20\" (1 2 !PLUS) \"5\" (1 3 !MINUS) (1 2 !SAME)";
        assertEquals("Math and Logic Chain", "@FALSE@", eval.evaluate(Parser.parse(input)));

        // Test: Concat
        // "Hello" "World" (2 1 !CONCAT)
        assertEquals("String Concat", "HelloWorld", eval.evaluate(Parser.parse("\"Hello\" \"World\" (2 1 !CONCAT)")));
    }

    private static void testNullCascade() {
        System.out.println("--- Testing Null Cascade ---");
        Evaluator eval = new Evaluator();

        // Node 2 is a failed PLUS (invalid numbers)
        // Node 3 references Node 2, should become null
        // Node 4 references Node 3, should become null
        String input = "\"Apple\" \"NotANumber\" (1 2 !PLUS) (1 2 !CONCAT)";
        assertEquals("Null Propagation", null, eval.evaluate(Parser.parse(input)));
    }

    private static void testKnowledgeRegistry() {
        System.out.println("--- Testing Knowledge Registry & Reification ---");
        Evaluator eval = new Evaluator();

        // Scenario: Sarah loves John. Sarah knows that Sarah loves John.
        // "John" "Sarah" (1 2 LOVES) (2 1 KNOWS)
        // Node 3: (Sarah LOVES John)
        // Node 4: (Sarah KNOWS (Sarah LOVES John))
        String input = "\"John\" \"Sarah\" (1 2 LOVES) (2 1 KNOWS)";
        String expected = "(Sarah KNOWS (Sarah LOVES John))";
        assertEquals("Reification Chain", expected, eval.evaluate(Parser.parse(input)));

        // Scenario: Check if the registry remembers the fact
        // ... (previous) "John" (1 4 ?KNOWS)
        // Does John know that Sarah knows he is loved?
        String input2 = "\"John\" \"Sarah\" (1 2 LOVES) (2 1 KNOWS) \"John\" (1 4 ?KNOWS)";
        // Node 6: (John KNOWS (Sarah KNOWS (Sarah LOVES John))) -> This creates a fact, returns it.
        // Wait, if we use ?KNOWS it returns T/F.
        // Check: Does John know the fact created at Node 4?
        // (4 5 ?KNOWS) -> Does Node 5 (John) know Node 4 (The fact)?
        String input3 = "\"John\" \"Sarah\" (1 2 LOVES) (2 1 KNOWS) \"John\" (2 1 ?KNOWS)";
        // Node 6: (John KNOWS (Sarah KNOWS (Sarah LOVES John))) ? -> F (because we never added this)
        assertEquals("Query Non-existent Fact", "@FALSE@", eval.evaluate(Parser.parse(input3)));
    }

    private static void testValueBasedIdentity() {
        System.out.println("--- Testing Value-Based Identity ---");
        Evaluator eval = new Evaluator();

        // Two different paths create the exact same fact.
        // "A" "B" (1 2 LINK) "A" "B" (1 2 LINK) (1 2 !SAME)
        // Node 3: (A LINK B)
        // Node 6: (A LINK B)
        // Node 7: (SAME(Node 6, Node 3)) -> Should be T because the strings are identical.
        String input = "\"A\" \"B\" (1 2 LINK) \"A\" \"B\" (1 2 LINK) (1 4 !SAME)";
        assertEquals("Identity Convergence", "@TRUE@", eval.evaluate(Parser.parse(input)));
    }

    private static void testComplexMerging() {
        System.out.println("--- Testing Complex Merging ---");
        
        // Donor: "Hello" <T1>
        // Acceptor: "World" "!" (1 2 !CONCAT) <<T1>> (1 3 !CONCAT)
        // 1. "World" (0)
        // 2. "!" (1)
        // 3. (World!) (2)
        // 4. <<T1>> (3)
        // 5. (Concat(<<T1>>, World!)) (4)
        
        String donorInput = "\"Hello\" <T1>";
        String acceptorInput = "\"World\" \"!\" (2 1 !CONCAT) <<T1>> (1 2 !CONCAT)";
        
        DAG donorDag = Parser.parse(donorInput);
        DAG acceptorDag = Parser.parse(acceptorInput);
        DAG merged = GraphMerger.merge(donorDag, acceptorDag);
        
        // Expected process:
        // Resulting DAG: ["Hello", <T1>, "World", "!", (World!), (Concat(T1, World!))]
        // <T1> becomes "Hello"
        // (World!) is "World!"
        // Final is "HelloWorld!"
        
        assertEquals("Deep Merge with Indices", "HelloWorld!", new Evaluator().evaluate(merged));
    }
}