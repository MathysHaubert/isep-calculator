package org.isep.cleancode.calculator;

import java.util.*;

public class Calculator {

    /**
     * Evaluates a mathematical expression given as a string using the Shunting-yard algorithm.
     *
     * @param expression the mathematical expression to evaluate
     * @return the result of the evaluation
     */
    public double evaluateMathExpression(String expression) {

        expression = expression.replaceAll("\\s+", "");

        List<String> tokens = tokenize(expression);

        List<String> rpn = toRPN(tokens);

        return evaluateRPN(rpn);
    }

    private List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder number = new StringBuilder();

        for (char c : expression.toCharArray()) {
            if (Character.isDigit(c) || c == '.') {
                number.append(c);
            } else {
                if (!number.isEmpty()) {
                    tokens.add(number.toString());
                    number.setLength(0);
                }
                tokens.add(String.valueOf(c));
            }
        }
        if (!number.isEmpty()) {
            tokens.add(number.toString());
        }

        return tokens;
    }

    private List<String> toRPN(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Stack<String> operators = new Stack<>();

        Map<String, Integer> precedence = Map.of(
                "+", 1,
                "-", 1,
                "*", 2
        );

        for (String token : tokens) {
            if (token.matches("\\d+(\\.\\d+)?")) {
                output.add(token);

            } else if ("+-*".contains(token)) {
                while (
                        !operators.isEmpty() &&
                        !operators.peek().equals("(") &&
                        precedence.getOrDefault(operators.peek(), 0) >= precedence.get(token)
                ) {
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
            if (token.matches("\\d+(\\.\\d+)?")) {
                stack.push(Double.parseDouble(token));
            } else {
                double b = stack.pop();
                double a = stack.pop();
                switch (token) {
                    case "+" -> stack.push(a + b);
                    case "-" -> stack.push(a - b);
                    case "*" -> stack.push(a * b);
                }
            }
        }

        return stack.pop();
    }
}
