package main;

import java.util.List;

import node.AcceptorNode;
import node.DonorNode;
import node.Node;
import node.PrimitiveCallNode;
import node.StringLiteralNode;

import java.util.*;



class Parser {

    /**
     * Main entry point: converts a textual description into a DAGProgram.
     */
    public DAGProgram parse(String input) throws SyntaxException {
        if (input == null || input.trim().isEmpty()) {
            return new DAGProgram(new ArrayList<>());
        }

        List<String> tokens = tokenize(input);
        List<Node> nodes = new ArrayList<>();
        
        int i = 0;
        while (i < tokens.size()) {
            String token = tokens.get(i);

            if (token.equals("(")) {
                // Handle Primitive Call: ( arg1 arg2 ... NAME )
                i = parsePrimitiveCall(tokens, i, nodes);
            } else if (token.startsWith("<<")) {
                // Handle Acceptor: <<TYPE_ID>>
                nodes.add(parseAcceptor(token));
                i++;
            } else if (token.startsWith("<")) {
                // Handle Donor: <TYPE_ID>
                nodes.add(parseDonor(token));
                i++;
            } else if (token.startsWith("\"")) {
                // Handle String Literal: "text"
                nodes.add(parseLiteral(token));
                i++;
            } else {
                throw new SyntaxException("Unexpected token at position " + i + ": " + token);
            }
        }

        return new DAGProgram(nodes);
    }

    /**
     * Breaks the input string into tokens.
     * Handles quoted strings as single tokens and separates parentheses.
     */
    private List<String> tokenize(String input) throws SyntaxException {
        List<String> tokens = new ArrayList<>();
        int n = input.length();
        int i = 0;

        while (i < n) {
            char c = input.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (c == '"') {
                // String Literal: read until closing quote
                StringBuilder sb = new StringBuilder();
                sb.append(c);
                i++;
                while (i < n && input.charAt(i) != '"') {
                    sb.append(input.charAt(i));
                    i++;
                }
                if (i >= n) throw new SyntaxException("Unclosed string literal");
                sb.append('"');
                tokens.add(sb.toString());
                i++;
            } else if (c == '(' || c == ')') {
                // Parentheses are distinct tokens
                tokens.add(String.valueOf(c));
                i++;
            } else if (c == '<') {
                // --- DECISION POINT: DONOR OR ACCEPTOR? ---
                
                // 1. Look ahead to decide the type
                boolean isAcceptor = (i + 1 < n && input.charAt(i + 1) == '<');
                StringBuilder sb = new StringBuilder();

                if (isAcceptor) {
                    // MODE: ACCEPTOR (<<TYPE_ID>>)
                    // Consume until we find the closing '>>'
                    while (i < n) {
                        sb.append(input.charAt(i));
                        i++;
                        if (sb.length() >= 2 && 
                            sb.charAt(sb.length() - 1) == '>' && 
                            sb.charAt(sb.length() - 2) == '>') {
                            break;
                        }
                    }
                    if (!sb.toString().endsWith(">>")) {
                        throw new SyntaxException("Malformed Acceptor: missing closing '>>'");
                    }
                } else {
                    // MODE: DONOR (<TYPE_ID>)
                    // Consume until we find the closing '>'
                    while (i < n) {
                        sb.append(input.charAt(i));
                        i++;
                        if (sb.charAt(sb.length() - 1) == '>') {
                            break;
                        }
                    }
                    if (!sb.toString().endsWith(">")) {
                        throw new SyntaxException("Malformed Donor: missing closing '>'");
                    }
                }
                tokens.add(sb.toString());
            }  else {
                // Regular alphanumeric tokens (Primitive names, arguments)
                StringBuilder sb = new StringBuilder();
                while (i < n && !Character.isWhitespace(input.charAt(i)) 
                       && input.charAt(i) != '(' && input.charAt(i) != ')' && input.charAt(i) != '"') {
                    sb.append(input.charAt(i));
                    i++;
                }
                tokens.add(sb.toString());
            }
        }
        return tokens;
    }

    private int parsePrimitiveCall(List<String> tokens, int startIdx, List<Node> nodes) throws SyntaxException {
        List<Integer> args = new ArrayList<>();
        int i = startIdx + 1;

        while (i < tokens.size() && !tokens.get(i).equals(")")) {
            String token = tokens.get(i);
            try {
                args.add(Integer.parseInt(token));
            } catch (NumberFormatException e) {
                // This might be the Primitive Name (the last token before ')')
                if (i == tokens.size() - 1 || tokens.get(i + 1).equals(")")) {
                    String primitiveName = token;
                    
                    // Convert List<Integer> to int[]
                    int[] argArray = new int[args.size()];
                    for (int j = 0; j < args.size(); j++) argArray[j] = args.get(j);
                    
                    nodes.add(new PrimitiveCallNode(primitiveName, argArray));
                    return i + 2; // Skip name and closing parenthesis
                } else {
                    throw new SyntaxException("Expected integer argument, found: " + token);
                }
            }
            i++;
        }
        throw new SyntaxException("Unclosed parenthesis in primitive call");
    }

    private String extractTypeId(String token, int prefixLen, int suffixLen) {
        return token.substring(prefixLen, token.length() - suffixLen);
    }

    private Node parseLiteral(String token) {
        // Remove the surrounding quotes
        String content = token.substring(1, token.length() - 1);
        return new StringLiteralNode(content);
    }

    private Node parseDonor(String token) {
        // Format: <TYPE_ID>
        String typeId = extractTypeId(token, 1, 1);
        return new DonorNode(typeId);
    }

    private Node parseAcceptor(String token) {
        // Format: <<TYPE_ID>>
        String typeId = extractTypeId(token, 2, 2);
        return new AcceptorNode(typeId);
    }
}