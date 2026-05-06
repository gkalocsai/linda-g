package node;


import java.util.List;

public class PrimitiveCall extends Node {
    private final List<Integer> args;
    private final String name;
    private final char mode; // '!', '?', or ' '

    public PrimitiveCall(List<Integer> args, char mode, String name) {
        this.args = args;
        this.mode = mode;
        this.name = name;
    }

    public List<Integer> getArgs() { return args; }
    public String getName() { return name; }
    public char getMode() { return mode; }
}