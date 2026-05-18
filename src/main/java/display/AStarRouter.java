package display;
import java.util.*;

public class AStarRouter {
    private static final char FIRST_CHAR = '\u25BA'; // ►
	
	private static final char LAST_CHAR = '\u25A0';   //■
	//private static final char FIRST_CHAR = '\u25A0';   //■
  
    // Box drawing characters
    private static final char HORIZ = '\u2500'; // ─
    private static final char VERT = '\u2502';  // │
    private static final char TL = '\u250C';    // ┌
    private static final char TR = '\u2510';    // ┐
    private static final char BL = '\u2514';    // └
    private static final char BR = '\u2518';    // ┘
  
    public static void routeLines(ColoredChar[][] canvas, List<Line> lines) {
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            
            List<Point> path = findPath(canvas, line.start, line.end);
  
            if (path != null) {
                drawPath(canvas, path, line.color);
            }
        }
    }
  
    private static List<Point> findPath(ColoredChar[][] canvas, Point start, Point end) {
        int rows = canvas.length;
        int cols = canvas[0].length;
  
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.fScore));
        Map<Point, Point> cameFrom = new HashMap<>();
        Map<Point, Integer> gScore = new HashMap<>();
  
        gScore.put(start, 0);
        openSet.add(new Node(start, heuristic(start, end)));
  
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            Point currPt = current.point;
  
            if (currPt.x == end.x && currPt.y == end.y) {
                return reconstructPath(cameFrom, end);
            }
  
            for (Point neighbor : getNeighbors(currPt, rows, cols)) {
                int tentativeGScore = gScore.get(currPt) + calculateCost(canvas, neighbor);
  
                if (tentativeGScore < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    cameFrom.put(neighbor, currPt);
                    gScore.put(neighbor, tentativeGScore);
                    int fScore = tentativeGScore + heuristic(neighbor, end);
                    openSet.add(new Node(neighbor, fScore));
                }
            }
        }
        return null;
    }
  
    private static int calculateCost(ColoredChar[][] canvas, Point p) {
        ColoredChar cell = canvas[p.y][p.x];
        if (cell == null) return 1;
  
        char c = cell.character;
        if (c == HORIZ || c == VERT) return 2;
        if (c == TL || c == TR || c == BL || c == BR) return 3;
        return 10; 
    }
  
    private static int heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
  
    private static List<Point> getNeighbors(Point p, int rows, int cols) {
        List<Point> neighbors = new ArrayList<>();
        if (p.x > 0) neighbors.add(new Point(p.x - 1, p.y));
        if (p.x < cols - 1) neighbors.add(new Point(p.x + 1, p.y));
        if (p.y > 0) neighbors.add(new Point(p.x, p.y - 1));
        if (p.y < rows - 1) neighbors.add(new Point(p.x, p.y + 1));
        return neighbors;
    }
  
    private static List<Point> reconstructPath(Map<Point, Point> cameFrom, Point current) {
        List<Point> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        return path;
    }
  
    private static void drawPath(ColoredChar[][] canvas, List<Point> path, String color) {
        for (int i = 0; i < path.size(); i++) {
            Point curr = path.get(i);
            char symbol;
  
            // Requirement: Every line must start with FIRST_CHAR and end with LAST_CHAR
            if (i == 0) {
                symbol = FIRST_CHAR;
            } else if (i == path.size() - 1) {
                symbol = LAST_CHAR;
            } else {
                Point prev = path.get(i - 1);
                Point next = path.get(i + 1);
                symbol = determineChar(prev, curr, next);
            }
            canvas[curr.y][curr.x] = new ColoredChar(symbol, color);
        }
    }
  
    private static char determineChar(Point prev, Point curr, Point next) {
        int dx1 = curr.x - prev.x;
        int dy1 = curr.y - prev.y;
        int dx2 = next.x - curr.x;
        int dy2 = next.y - curr.y;
  
        // Straight lines
        if (dx1 != 0 && dx2 != 0) return HORIZ;
        if (dy1 != 0 && dy2 != 0) return VERT;
  
        // Corner logic:
        // From left (dx1=1), then go down (dy2=1) -> Top Right ┐
        if (dx1 == 1 && dy2 == 1) return TR;
        // From right (dx1=-1), then go down (dy2=1) -> Top Left ┌
        if (dx1 == -1 && dy2 == 1) return TL;
        // From left (dx1=1), then go up (dy2=-1) -> Bottom Right ┘
        if (dx1 == 1 && dy2 == -1) return BR;
        // From right (dx1=-1), then go up (dy2=-1) -> Bottom Left └
        if (dx1 == -1 && dy2 == -1) return BL;
        
        // From top (dy1=1), then go right (dx2=1) -> Bottom Left └
        if (dy1 == 1 && dx2 == 1) return BL;
        // From top (dy1=1), then go left (dx2=-1) -> Bottom Right ┘
        if (dy1 == 1 && dx2 == -1) return BR;
        // From bottom (dy1=-1), then go right (dx2=1) -> Top Left ┌
        if (dy1 == -1 && dx2 == 1) return TL;
        // From bottom (dy1=-1), then go left (dx2=-1) -> Top Right ┐
        if (dy1 == -1 && dx2 == -1) return TR;
  
        return ' ';
    }
  
    private static class Node {
        Point point;
        int fScore;
        Node(Point point, int fScore) {
            this.point = point;
            this.fScore = fScore;
        }
    }
}
  

