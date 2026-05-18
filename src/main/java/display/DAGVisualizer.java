package display;

import node.*;

import java.awt.Color;
import java.util.*;

import main.DAG;

public class DAGVisualizer {
    private static final String[] COLOR_PALETTE = {"\u001B[31m", "\u001B[32m", "\u001B[33m", "\u001B[34m", "\u001B[37m"};
    private static final int MIDLINE = 5;
    private static final int GRID_HEIGHT = 11;
    private static final int START_OFFSET = 2;
    private static final int NODE_GAP = 6;

    public String visualize(DAG dag) {
        List<Node> nodes = dag.getNodes();
        Map<Node, Color> nodeColorMap = new HashMap<>();
        Map<Node, Integer> xCoords = new HashMap<>();

        // 1. Layout Calculation
        int currentX = START_OFFSET;
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            Color color = null;
            if(node instanceof PlaceHolder) {
            	color = ColorTable.getOne(((PlaceHolder) node).getTypeId().hashCode());
            }else if(node instanceof Literal) {
            	color = ColorTable.getOne(((Literal) node).getResult().hashCode());
            }
            else {
            	color = ColorTable.getOne(node.getClass().hashCode());
            }
            nodeColorMap.put(node, color);
            xCoords.put(node, currentX);
            currentX += node.toString().length() + NODE_GAP;
        }

        List<Line> connections = calculateConnections(dag, xCoords, nodeColorMap);
        
        ColoredChar[][] canvas = new ColoredChar[GRID_HEIGHT][currentX + 1];

        // 2. Draw Node Labels
        for (Node node : nodes) {
            int x = xCoords.get(node);
            String label = node.toString();
            Color color = nodeColorMap.get(node);

            for (int j = 0; j < label.length(); j++) {
                canvas[MIDLINE][x + j] = new ColoredChar(label.charAt(j), color);
            }
        }

        // 3. Total Delegation to Renderer
        AStarRouter.routeLines(canvas, connections);

        // 4. Clip the canvas and return as String
        return getClippedCanvasAsString(canvas);
    }

    private String getClippedCanvasAsString(ColoredChar[][] canvas) {
        int minX = canvas[0].length, maxX = 0;
        int minY = canvas.length, maxY = 0;
        boolean hasContent = false;

        for (int y = 0; y < canvas.length; y++) {
            for (int x = 0; x < canvas[y].length; x++) {
                if (canvas[y][x] != null) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                    hasContent = true;
                }
            }
        }

        if (!hasContent) return "";

        StringBuilder sb = new StringBuilder();
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                sb.append(canvas[y][x] == null ? " " : canvas[y][x].toString());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public List<Line> calculateConnections(DAG dag, Map<Node, Integer> xCoords, Map<Node, Color> nodeColorMap) {
        List<Line> connections = new ArrayList<>();
        List<Node> allNodes = dag.getNodes();

        for (int i = 0; i < allNodes.size(); i++) {
            Node node = allNodes.get(i);
            int x = xCoords.get(node);
            List<Node> sources = resolveInputSources(node, i, allNodes);
            List<Point> inputPoints = getInputCoordinates(node, x);

            for (int j = 0; j < sources.size(); j++) {
                Node sourceNode = sources.get(j);
                int sourceX = xCoords.get(sourceNode);
                Point start = getOutputCoordinates(sourceX, sourceNode.toString().length());

                if (j < inputPoints.size()) {
                    connections.add(new Line(start, inputPoints.get(j), nodeColorMap.get(sourceNode)));
                }
            }
        }
        return connections;
    }

    public Point getOutputCoordinates(int nodeX, int width) {
        return new Point(nodeX + width, MIDLINE);
    }

    public List<Point> getInputCoordinates(Node node, int nodeX) {
        List<Point> coords = new ArrayList<>();
        int arity = node.getArity();
        int x = nodeX - 1;
        if (arity == 1) coords.add(new Point(x, MIDLINE));
        else if (arity == 2) {
            coords.add(new Point(x, MIDLINE - 1));
            coords.add(new Point(x, MIDLINE + 1));
        } else {
            coords.add(new Point(x, MIDLINE - 2));
            coords.add(new Point(x, MIDLINE));
            coords.add(new Point(x, MIDLINE + 2));
        }
        return coords;
    }

    private List<Node> resolveInputSources(Node node, int index, List<Node> allNodes) {
        List<Node> sources = new ArrayList<>();
        if (node instanceof PrimitiveCall) sources.addAll(((PrimitiveCall) node).getArgs());
        else if (node instanceof PlaceHolder) {
            Node input = ((PlaceHolder) node).getInput();
            if (input != null) sources.add(input);
        }
        return sources;
    }

    public static void main(String[] args) {
        DAG dag = new DAG();

        Literal lit1 = new Literal("1");
        Donor donor = new Donor("A");

        List<Node> concatArgs = new ArrayList<>();
        concatArgs.add(lit1);
        concatArgs.add(donor);
        PrimitiveCall concat = new PrimitiveCall(concatArgs, '!', "CONCAT");

        Acceptor acceptor = new Acceptor("B");

        dag.addNode(lit1);
        dag.addNode(donor);
        dag.addNode(concat);
        dag.addNode(acceptor);

        acceptor.setInput(lit1);

        DAGVisualizer visualizer = new DAGVisualizer();
        System.out.println("DAG Visualization (Using explicit PlaceHolder inputs):\n");
        System.out.println(visualizer.visualize(dag));
    }
}