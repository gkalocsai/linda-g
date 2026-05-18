package display;


public class Line {
    public final Point start;
    public final Point end;
    public final String color;

    public Line(Point start, Point end, String color) {
        this.start = start;
        this.end = end;
        this.color = color;
    }

    @Override
    public String toString() {
        return String.format("Edge[Start: %s, End: %s, Color: %s]", start, end, color);
    }
}