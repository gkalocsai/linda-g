package display;
public class ColoredChar {
    public char character;
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