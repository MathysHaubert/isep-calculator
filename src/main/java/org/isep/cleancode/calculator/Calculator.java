package org.isep.cleancode.calculator;

import java.util.*;

public class Calculator {

    private static final String REGEX_REPLACE_UNARY = "(?<=^|[\\(\\+\\-\\*/])\\s*-\\s*(\\d+(?:\\.\\d+)?)";
    private static final String REGEX_REPLACE_UNARY_BEFORE_PARENTHISES = "(?<=^|[\\(\\+\\-\\*/])\\s*-\\s*\\(";
    private static final String REGEX_REPLACE_WHITESPACE = "\\s+";

    /**
     * Évalue une expression mathématique au format chaîne en utilisant l’algorithme de Shunting-yard.
     *
     * @param expression L'expression mathématique à évaluer.
     * @return Le résultat numérique de l’évaluation.
     */
    public double evaluateMathExpression(String expression) {
        
        expression = expression.trim();
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("Expression vide");
        }
        if (expression.matches(".*\\d+\\s+\\d+.*")) {
            throw new IllegalArgumentException("Deux nombres consécutifs sans opérateur détectés");
        }

        expression = expression.replaceAll(REGEX_REPLACE_WHITESPACE, "");
        expression = expression.replaceAll(REGEX_REPLACE_UNARY, "~$1");
        expression = expression.replaceAll(REGEX_REPLACE_UNARY_BEFORE_PARENTHISES, "~(");

        List<String> tokens = tokenize(expression);
        List<String> rpn = toRPN(tokens);
        return evaluateRPN(rpn);
    }

    private List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            if (Character.isDigit(c) || c == '.' || c == '~') {
                number.append(c);
            } else {
                
                if (!number.isEmpty()) {
                    String num = number.toString();
                    number.setLength(0);
                    if (num.startsWith("~")) {
                        
                        String value = num.substring(1);
                        tokens.add("(");
                        tokens.add("0");
                        tokens.add("-");
                        tokens.add(value);
                        tokens.add(")");
                    } else {
                        tokens.add(num);
                    }
                }
                
                if ("+-*/()".indexOf(c) >= 0) {
                    tokens.add(String.valueOf(c));
                } else {
                    throw new IllegalArgumentException("Invalid character: " + c);
                }
            }
        }
        
        if (!number.isEmpty()) {
            String num = number.toString();
            if (num.startsWith("~")) {
                String value = num.substring(1);
                tokens.add("(");
                tokens.add("0");
                tokens.add("-");
                tokens.add(value);
                tokens.add(")");
            } else {
                tokens.add(num);
            }
        }

        return tokens;
    }

    private List<String> toRPN(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Stack<String> operators = new Stack<>();

        Map<String, Integer> precedence = Map.of(
                "+", 1,
                "-", 1,
                "*", 2,
                "/", 2
        );

        for (String token : tokens) {
            if (token.matches("\\d+(\\.\\d+)?")) {
                output.add(token);
            } else if ("+-*/".contains(token)) {
                int tokenPrecedence = precedence.getOrDefault(token, -1);
                while (!operators.isEmpty() && !operators.peek().equals("(") &&
                        precedence.getOrDefault(operators.peek(), 0) >= tokenPrecedence) {
                    output.add(operators.pop());
                }
                operators.push(token);
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    output.add(operators.pop());
                }
                if (!operators.isEmpty() && operators.peek().equals("(")) {
                    operators.pop();
                } else {
                    throw new IllegalArgumentException("Mismatched parentheses");
                }
            }
        }

        while (!operators.isEmpty()) {
            if (operators.peek().equals("(")) {
                throw new IllegalArgumentException("Mismatched parentheses");
            }
            output.add(operators.pop());
        }

        return output;
    }

    private double evaluateRPN(List<String> rpn) {
        Stack<Double> stack = new Stack<>();

        for (String token : rpn) {
            if (token.matches("-?\\d+(\\.\\d+)?")) {
                stack.push(Double.parseDouble(token));
            } else {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Expression invalide : pas assez d'opérandes pour '" + token + "'");
                }
                double b = stack.pop();
                double a = stack.pop();
                switch (token) {
                    case "+" -> stack.push(a + b);
                    case "-" -> stack.push(a - b);
                    case "*" -> stack.push(a * b);
                    case "/" -> {
                        if (b == 0) {
                            throw new ArithmeticException("Division par zéro");
                        }
                        stack.push(a / b);
                    }
                    default -> throw new IllegalArgumentException("Opérateur inconnu: " + token);
                }
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Expression invalide : trop d'opérandes");
        }

        return stack.pop();
    }
}
