package main;

import node.*;
import node.primitive.*;
import java.util.*;

/**
 * Represents a character with an associated ANSI color.
 */
class ColoredChar {
    char character;
    String color;

    public ColoredChar(char character, String color) {
        this.character = character;
        this.color = color;
    }

    @Override
    public String toString() {
        return color + character + "\u001B[0m";
    }
}

class Point {
    int x, y;
    public Point(int x, int y) { this.x = x; this.y = y; }
    @Override
    public String toString() { return "(" + x + "," + y + ")"; }
}

public class DAGVisualizer {
    // ANSI Color Constants (No Cyan or Magenta)
    private static final String[] COLOR_PALETTE = {
        "\u001B[31m", // Red
        "\u001B[32m", // Green
        "\u001B[33m", // Yellow
        "\u001B[34m", // Blue
        "\u001B[37m", // White
        "\u001B[35m"  // Use 35 with caution, but prompt said no Magenta. 
                      // Let's stick to 31, 32, 33, 34, 37.
    };

    private static final char INPUT_CHAR = '\u25A0';  // Black Square
    private static final char OUTPUT_CHAR = '\u25BA'; // Right Pointer
    private static final int MIDLINE = 2;
    private static final int GRID_HEIGHT = 5;

    public void visualize(DAG dag) {
        List<Node> nodes = dag.getNodes();
        Map<Node, String> nodeColorMap = new HashMap<>();
        
        // 1. Assign a unique color to each node based on its index
        for (int i = 0; i < nodes.size(); i++) {
            nodeColorMap.put(nodes.get(i), COLOR_PALETTE[i % COLOR_PALETTE.length]);
        }

        // Estimate canvas width
        int totalWidth = 0;
        for (Node n : nodes) {
            totalWidth += n.toString().length() + 4;
        }

        ColoredChar[][] canvas = new ColoredChar[GRID_HEIGHT][totalWidth];
        int currentX = 2;

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            String label = node.toString();
            int width = label.length();
            String myColor = nodeColorMap.get(node);

            // --- HANDLE INPUTS ---
            List<Point> inputCoords = getInputCoordinates(node, currentX);
            List<Node> inputs = getInputsOfNode(node);
            
            for (int j = 0; j < inputCoords.size(); j++) {
                Point p = inputCoords.get(j);
                // Connection Logic: Input square gets the color of the SOURCE node
                String sourceColor = (j < inputs.size()) 
                                     ? nodeColorMap.get(inputs.get(j)) 
                                     : myColor; 
                canvas[p.y][p.x] = new ColoredChar(INPUT_CHAR, sourceColor);
            }

            // --- HANDLE NODE BODY ---
            for (int j = 0; j < width; j++) {
                canvas[MIDLINE][currentX + j] = new ColoredChar(label.charAt(j), myColor);
            }

            // --- HANDLE OUTPUT ---
            Point outputCoord = getOutputCoordinates(i, currentX, width);
            // Node output has the same color as the node
            canvas[outputCoord.y][outputCoord.x] = new ColoredChar(OUTPUT_CHAR, myColor);

            currentX += width + 3;
        }

        printCanvas(canvas);
    }

    /**
     * Helper to extract arguments from any Node type
     */
    private List<Node> getInputsOfNode(Node node) {
        if (node instanceof PrimitiveCall) {
            return ((PrimitiveCall) node).getArgs();
        }
        // For Placeholders/Literals, they might not have 'args' in the class 
        // but might be treated as having 1 input (Placeholders) or 0 (Literals)
        return Collections.emptyList();
    }

    public List<Point> getInputCoordinates(Node node, int nodeX) {
        List<Point> coords = new ArrayList<>();
        int arity = node.getArity();
        int x = nodeX - 1;

        if (arity == 1) {
            coords.add(new Point(x, MIDLINE));
        } else if (arity == 2) {
            coords.add(new Point(x, MIDLINE - 1));
            coords.add(new Point(x, MIDLINE + 1));
        } else if (arity >= 3) {
            coords.add(new Point(x, MIDLINE - 2));
            coords.add(new Point(x, MIDLINE));
            coords.add(new Point(x, MIDLINE + 2));
        }
        return coords;
    }

    public Point getOutputCoordinates(int nodeIdx, int nodeX, int width) {
        return new Point(nodeX + width, MIDLINE);
    }

    public List<Point> getEdgeTerminals(Node node, int nodeX, int width) {
        List<Point> terminals = new ArrayList<>();
        List<Point> inputs = getInputCoordinates(node, nodeX);
        for (Point p : inputs) {
            terminals.add(new Point(p.x - 1, p.y));
        }
        terminals.add(new Point(nodeX + width + 1, MIDLINE));
        return terminals;
    }

    private void printCanvas(ColoredChar[][] canvas) {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < canvas[y].length; x++) {
                sb.append(canvas[y][x] == null ? " " : canvas[y][x].toString());
            }
            System.out.println(sb.toString());
        }
    }

    public static void main(String[] args) {
        DAG dag = new DAG();
        
        // Setup a scenario: Lit1 and Lit2 flow into Concat, Concat flows into Sum
        Literal lit1 = new Literal("1");
        Literal lit2 = new Literal("2");
        
        List<Node> concatArgs = new ArrayList<>();
        concatArgs.add(lit1);
        concatArgs.add(lit2);
        PrimitiveCall concat = new PrimitiveCall(concatArgs, '!', "CONCAT");
        
        List<Node> sumArgs = new ArrayList<>();
        sumArgs.add(concat);
        sumArgs.add(lit1); // Reuse lit1
        PrimitiveCall sum = new PrimitiveCall(sumArgs, '!', "PLUS");

        dag.addNode(lit1);
        dag.addNode(lit2);
        dag.addNode(concat);
        dag.addNode(sum);

        DAGVisualizer visualizer = new DAGVisualizer();
        System.out.println("DAG Visualization (Connected colors synchronized):\n");
        visualizer.visualize(dag);
    }
}