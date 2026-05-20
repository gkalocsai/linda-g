package main;

import display.DAGVisualizer;

public class LindaGDemo {
    public static void main(String[] args) {
   
        Evaluator evaluator = new Evaluator();
        KnowledgeBase kb = getKnowledgeBaseFromEvaluator(evaluator);

        // Add some data to the KB: The item "Laptop" costs 1000
        kb.addTriple("@Laptop@", "price", "1000");

        String donorString = "@UNKNOWN@ \"price\" @Laptop@  (1 2 3 !FIND) \"50\" (1 2 !PLUS) <PriceValue>";
        
        String acceptorString = "\"2\" <<PriceValue>> (1 2 !PLUS) \" USD\" (2 1 !CONCAT)";

        // 4. Parse
        DAG donorDag = Parser.parse(donorString);
        DAG acceptorDag = Parser.parse(acceptorString);

        // 5. Merge
        System.out.println("\n--- Merging Graphs... ---");
        DAG mergedDag = GraphMerger.merge(donorDag, acceptorDag);
        System.out.println("Merged DAG Structure: " + mergedDag.toString());

      
        // 6. Evaluate
        System.out.println("\n--- Evaluating Result ---");
        String finalResult = evaluator.evaluate(mergedDag);
        System.out.println("Final Output: " + finalResult);
        
        System.out.println(DAGVisualizer.visualize(mergedDag));
        
    }

    // Helper to access the private KB in Evaluator via reflection for the demo
    private static KnowledgeBase getKnowledgeBaseFromEvaluator(Evaluator eval) {
        try {
            java.lang.reflect.Field field = Evaluator.class.getDeclaredField("kb");
            field.setAccessible(true);
            return (KnowledgeBase) field.get(eval);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}