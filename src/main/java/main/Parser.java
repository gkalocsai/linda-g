package main;

import node.*;
import java.util.*;
import java.util.regex.*;

public class Parser {
    public static DAG parse(String input) {
        DAG dag = new DAG();
        // Basic tokenizer that respects quoted strings
        List<String> tokens = tokenize(input);
        
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.startsWith("\"")) {
                Literal lit = new Literal(token.substring(1, token.length() - 1)); // Generic node as Literal                
                dag.addNode(lit);
            } else if (token.startsWith("<<")) {
                dag.addNode(new Acceptor(token.substring(2, token.length() - 2)));
            } else if (token.startsWith("<")) {
                dag.addNode(new Donor(token.substring(1, token.length() - 1)));
            } else if (token.startsWith("(")) {
                // Handle primitive calls which may span multiple tokens
                StringBuilder sb = new StringBuilder(token);
                while (!sb.toString().endsWith(")")) {
                    sb.append(" ").append(tokens.get(++i));
                }
                dag.addNode(parsePrimitive(sb.toString()));
            }
        }
        return dag;
    }

    private static Node parsePrimitive(String content) {
        String inner = content.substring(1, content.length() - 1).trim();
        String[] parts = inner.split("\\s+");
        
        List<Integer> args = new ArrayList<>();
        String name = "";
        char mode = ' ';

        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.matches("\\d+")) {
                args.add(Integer.parseInt(p));
            } else {
                if (p.startsWith("!")) { mode = '!'; name = p.substring(1); }
                else if (p.startsWith("?")) { mode = '?'; name = p.substring(1); }
                else { mode = ' '; name = p; }
            }
        }
        return new PrimitiveCall(args, mode, name);
    }

    private static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        Matcher m = Pattern.compile("\"[^\"]*\"|<<[^>>]*>>|<[^>]*>|\\([^)]*\\)|\\S+").matcher(input);
        while (m.find()) tokens.add(m.group());
        return tokens;
    }
}