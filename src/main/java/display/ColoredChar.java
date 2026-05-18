package display;

import java.awt.Color;


public class ColoredChar {
    public char character;
    public Color color; // Changed from String to Color

    public ColoredChar(char character, Color color) {
        this.character = character;
        this.color = color;
    }

    @Override
    public String toString() {
        // Use the Colorizer to wrap this specific character in ANSI codes
        return Colorizer.colorize(String.valueOf(character), color);
    }
}