package node;
import java.util.List;

public class PrimitiveCall implements Node {
    private final List<Node> args;
    private final String name;
    private final char mode; // '!', '?', or ' '
    private String result;

    public PrimitiveCall(List<Node> args, char mode, String name) {
        this.args = args;
        this.mode = mode;
        this.name = name;
    }

    @Override
    public int getArity() {
        return args.size();
    }

    public List<Node> getArgs() { return args; }
    public String getName() { return name; }
    public char getMode() { return mode; }

    @Override
    public void setResult(String str) {
        this.result = str;
    }

    @Override
    public String getResult() {
        return this.result;
    }
}