package main;
import node.*;
import java.util.*;
import java.util.regex.*;

public class Parser {
    public static DAG parse(String input) {
        DAG dag = new DAG();
        List<String> tokens = tokenize(input);
        List<Node> nodes = dag.getNodes();

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.startsWith("\"")) {
                nodes.add(new Literal(token.substring(1, token.length() - 1)));
            } else if (token.startsWith("<<")) {
                nodes.add(new Acceptor(token.substring(2, token.length() - 2)));
            } else if (token.startsWith("<")) {
                nodes.add(new Donor(token.substring(1, token.length() - 1)));
            } else if (token.startsWith("(")) {
                StringBuilder sb = new StringBuilder(token);
                while (!sb.toString().endsWith(")")) {
                    sb.append(" ").append(tokens.get(++i));
                }
                // Pass current nodes list and current index to resolve relative references
                nodes.add(parsePrimitive(sb.toString(), nodes));
            }
        }
        return dag;
    }

    private static Node parsePrimitive(String content, List<Node> currentNodes) {
        String inner = content.substring(1, content.length() - 1).trim();
        String[] parts = inner.split("\\s+");
        List<Node> resolvedArgs = new ArrayList<>();
        String name = "";
        char mode = ' ';
        int currentPos = currentNodes.size();

        for (String p : parts) {
            if (p.matches("\\d+")) {
                int rel = Integer.parseInt(p);
                int targetIdx = currentPos - rel;
                if (targetIdx >= 0 && targetIdx < currentNodes.size()) {
                    resolvedArgs.add(currentNodes.get(targetIdx));
                }
            } else {
                if (p.startsWith("!")) { mode = '!'; name = p.substring(1); }
                else if (p.startsWith("?")) { mode = '?'; name = p.substring(1); }
                else { mode = ' '; name = p; }
            }
        }
        return new PrimitiveCall(resolvedArgs, mode, name);
    }

    private static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        Matcher m = Pattern.compile("\"[^\"]*\"|<<[^>>]*>>|<[^>]*>|\\([^)]*\\)|\\S+").matcher(input);
        while (m.find()) tokens.add(m.group());
        return tokens;
    }
}